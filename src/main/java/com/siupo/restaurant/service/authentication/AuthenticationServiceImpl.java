package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.UserDTO;
import com.siupo.restaurant.dto.request.*;
import com.siupo.restaurant.dto.response.LoginDataResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;
import com.siupo.restaurant.exception.BadRequestException;
import com.siupo.restaurant.exception.UnauthorizedException;
import com.siupo.restaurant.model.Customer;
import com.siupo.restaurant.model.RefreshToken;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.RefreshTokenRepository;
import com.siupo.restaurant.repository.UserRepository;
import com.siupo.restaurant.security.JwtUtils;
import com.siupo.restaurant.service.mail.EmailService;
import jakarta.mail.MessagingException;
import jakarta.persistence.DiscriminatorValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    private final Map<String, Pending<RegisterRequest>> pendingRegisters = new ConcurrentHashMap<>();
    private final Map<String, Pending<String>> pendingForgotPasswords = new ConcurrentHashMap<>();

    @Getter
    @RequiredArgsConstructor
    private static class Pending<T> {
        private final T dataRequest;
        private final String otpHash;
        private final Instant expiryTime;
        private int attempts = 5;

        public boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }
        public boolean attempts() { return attempts >0; }
    }

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     RefreshTokenRepository refreshTokenRepository,
                                     JwtUtils jwtUtils,
                                     BCryptPasswordEncoder passwordEncoder,
                                     EmailService emailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // =============== ĐĂNG KÝ ===============
    @Override
    public MessageDataReponse register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent())
            return new MessageDataReponse(false,"400","Email đã tồn tại!");

        Pending<RegisterRequest> existing = pendingRegisters.get(registerRequest.getEmail());
        if (existing != null && !existing.isExpired() && existing.attempts()) {
            return new MessageDataReponse(true,"200","Vui lòng kiểm tra email, mã OTP vẫn còn hiệu lực!");
        }

        String otp = generateOTP();
        String otpHash = passwordEncoder.encode(otp);

        pendingRegisters.put(registerRequest.getEmail(),
                new Pending<RegisterRequest>(registerRequest, otpHash, Instant.now().plusSeconds(300)));

        return sendEmail(registerRequest.getEmail(), otp);
    }

    // =============== XÁC NHẬN OTP ===============
    @Override
    public MessageDataReponse confirmRegistration(String email, String otp) {
        Pending<RegisterRequest> pendingUser = pendingRegisters.get(email);

        if (pendingUser == null || pendingUser.isExpired()) {
            pendingRegisters.remove(email);
            return new MessageDataReponse(false,"400","Yêu cầu đăng ký không tồn tại hoặc đã hết hạn!");
        }


        if (pendingUser.attempts <= 0) {
            pendingRegisters.remove(email);
            return new MessageDataReponse(false,"400","Bạn đã nhập sai OTP quá 5 lần, vui lòng đăng ký lại!");
        }

        if (!passwordEncoder.matches(otp, pendingUser.getOtpHash())) {
            pendingUser.attempts--;
            Map<String, Object> data = new HashMap<>();
            data.put("attempt", pendingUser.attempts);
            data.put("message", "Bạn còn lại " + pendingUser.attempts + " lượt");
            return new MessageDataReponse(false,"400","OTP không đúng!",data);
        }

        RegisterRequest req = pendingUser.getDataRequest();
        Customer newUser = Customer.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        userRepository.save(newUser);
        pendingRegisters.remove(email);
        return new MessageDataReponse(true,"200","Xác thực thành công! Tài khoản đã được tạo.");
    }

    // =============== GỬI LẠI OTP ===============
    @Override
    public void resendOtp(String email) {
        Pending<RegisterRequest> pendingRegister = pendingRegisters.get(email);
        Pending<String> pendingForgotPassword = pendingForgotPasswords.get(email);

        if (pendingRegister == null && pendingForgotPassword == null)
            throw new BadRequestException("Không tìm thấy yêu cầu đăng ký nào cho email này!");

        // Tạo OTP mới
        String newOtp = generateOTP();
        String newOtpHash = passwordEncoder.encode(newOtp);
        Instant newExpiry = Instant.now().plusSeconds(300);

        if(pendingRegister != null)
            pendingRegisters.put(email, new Pending<RegisterRequest>(pendingRegister.getDataRequest(), newOtpHash, newExpiry));
        else
            pendingForgotPasswords.put(email, new Pending<String>(email, newOtpHash, newExpiry));
        try {
            emailService.sendOTPToEmail(email, newOtp);
        } catch (MessagingException e) {
            throw new BadRequestException("Gửi lại email OTP thất bại!");
        }
    }

    // =============== DỌN OTP HẾT HẠN ===============
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredPendingUsers() {
        pendingRegisters.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // =============== ĐĂNG NHẬP ===============
    @Override
    @Transactional
    public LoginDataResponse login(LoginRequest loginRequest) {
        // 1. Lấy user theo email
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        if (userOpt.isEmpty()) {
            return LoginDataResponse.builder()
                    .message("Đăng nhập thất bại: Tài khoản không tồn tại")
                    .accessToken(null)
                    .refreshToken(null)
                    .user(null)
                    .build();
        }
        User user = userOpt.get();

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return LoginDataResponse.builder()
                    .message("Đăng nhập thất bại: Mật khẩu không đúng")
                    .accessToken(null)
                    .refreshToken(null)
                    .user(null)
                    .build();
        }

        // 3. Revoke các refresh token cũ
        List<RefreshToken> existingTokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        existingTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAllAndFlush(existingTokens);

        // 4. Tạo access token mới
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());

        // 5. Tạo refresh token mới, đảm bảo unique
        String refreshTokenValue;
        do {
            refreshTokenValue = UUID.randomUUID().toString();
        } while (refreshTokenRepository.existsByToken(refreshTokenValue));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        String userRole = getUserRole(user);

        // 6. Convert User sang UserDTO
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(userRole)
                .build();

        // 7. Trả về LoginDataResponse
        return LoginDataResponse.builder()
                .message("Đăng nhập thành công")
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .user(userDTO)
                .build();
    }

    /**
     * ✅ Phương thức an toàn để lấy role từ User
     * Hỗ trợ nhiều cách lấy role khác nhau
     */
    private String getUserRole(User user) {
        // Cách 1: Lấy từ @DiscriminatorValue annotation (nếu có)
        DiscriminatorValue discriminatorValue = user.getClass().getAnnotation(DiscriminatorValue.class);
        if (discriminatorValue != null) {
            return discriminatorValue.value();
        }

        // Cách 2: Lấy từ simple class name
        // Customer -> CUSTOMER, Admin -> ADMIN, Staff -> STAFF
        String className = user.getClass().getSimpleName();
        return className.toUpperCase();
    }

    // =============== REFRESH TOKEN ===============
    @Override
    @Transactional
    public LoginDataResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findActiveByToken(requestRefreshToken, Instant.now())
                .orElseThrow(() -> new UnauthorizedException("Refresh token không hợp lệ hoặc đã hết hạn!"));

        User user = refreshToken.getUser();

        // Tạo access token mới
        String newAccessToken = jwtUtils.generateAccessToken(user.getEmail());

        // Token rotation: tạo refresh token mới
        String newRefreshTokenValue = UUID.randomUUID().toString();

        // Revoke refresh token cũ
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Tạo refresh token mới
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return LoginDataResponse.builder()
                .message("Refresh token thành công")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .build();
    }

    // =============== ĐĂNG XUẤT ===============
    @Override
    @Transactional
    public void logout(LogoutRequest logoutRequest) {
        String refreshTokenValue = logoutRequest.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Refresh token không tồn tại!"));

        // Đánh dấu refresh token là revoked
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    // =============== DỌN DẸP TOKEN HẾT HẠN VÀ REVOKED ===============
    @Scheduled(fixedRate = 3600000) // Chạy mỗi giờ
    @Transactional
    public void cleanupTokens() {
        // Xóa expired tokens
        refreshTokenRepository.deleteExpiredTokens(Instant.now());

        // Xóa revoked tokens
        refreshTokenRepository.deleteRevokedTokens();

        System.out.println("🧹 Đã dọn dẹp refresh tokens hết hạn và revoked");
    }

    // =============== HÀM TẠO MÃ OTP ===============
    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    @Override
    public MessageDataReponse requestForgotPassword(String email) {
        if (!userRepository.findByEmail(email).isPresent())
            return new MessageDataReponse(false,"400","Email chưa được đăng ký!");

        Pending<String> existing = pendingForgotPasswords.get(email);
        if (existing != null && !existing.isExpired() && existing.attempts()) {
            return new MessageDataReponse(true,"200","Vui lòng kiểm tra email, mã OTP vẫn còn hiệu lực!");
        }

        String otp = generateOTP();
        String otpHash = passwordEncoder.encode(otp);

        pendingForgotPasswords.put(email,
                new Pending<String>(email, otpHash, Instant.now().plusSeconds(300)));

        return sendEmail(email, otp);
    }

    @Override
    public MessageDataReponse setNewPassword(ForgotPasswordRequest forgotPasswordRequest) {
        Pending<String> pendingRequest = pendingForgotPasswords.get(forgotPasswordRequest.getEmail());

        if (pendingRequest == null || pendingRequest.isExpired()) {
            pendingForgotPasswords.remove(forgotPasswordRequest.getEmail());
            return new MessageDataReponse(false,"400","Yêu cầu đặt lại mật khẩu không tồn tại hoặc đã hết hạn!");
        }

        if (pendingRequest.attempts <= 0) {
            pendingForgotPasswords.remove(forgotPasswordRequest.getEmail());
            return new MessageDataReponse(false,"400","Bạn đã nhập sai OTP quá 5 lần, vui lòng thử lại!");
        }

        if (!passwordEncoder.matches(forgotPasswordRequest.getOtp(), pendingRequest.getOtpHash())) {
            pendingRequest.attempts--;
            Map<String, Object> data = new HashMap<>();
            data.put("attempt", pendingRequest.attempts);
            data.put("message", "Bạn còn lại " + pendingRequest.attempts + " lượt");
            return new MessageDataReponse(false,"400","OTP không đúng!",data);
        }

        User user = userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Người dùng không tồn tại!"));

        user.setPassword(passwordEncoder.encode(forgotPasswordRequest.getNewPassword()));
        userRepository.save(user);
        pendingForgotPasswords.remove(forgotPasswordRequest.getEmail());
        return new MessageDataReponse(true,"200","Đặt lại mật khẩu thành công!");
    }

    private MessageDataReponse sendEmail(String email, String otp) {
        try {
            if (emailService.sendOTPToEmail(email, otp))
                return new MessageDataReponse(true, "201", "Đã gửi mã OTP tới email!");
            else {
                pendingForgotPasswords.remove(email);
                return new MessageDataReponse(false, "400", "Không thể gửi email OTP, vui lòng thử lại!");
            }

        }
        catch (MessagingException e) {
            pendingForgotPasswords.remove(email);
            return new MessageDataReponse(false, "400", "Không thể gửi email OTP, vui lòng thử lại!");
        }
    }
}