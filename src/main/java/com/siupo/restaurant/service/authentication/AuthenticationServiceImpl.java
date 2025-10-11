package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.LoginRequest;
import com.siupo.restaurant.dto.request.RegisterRequest;
import com.siupo.restaurant.dto.request.RefreshTokenRequest;
import com.siupo.restaurant.dto.request.LogoutRequest;
import com.siupo.restaurant.dto.response.LoginDataResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;
import com.siupo.restaurant.exception.BadRequestException;
import com.siupo.restaurant.exception.UnauthorizedException;
import com.siupo.restaurant.model.RefreshToken;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.RefreshTokenRepository;
import com.siupo.restaurant.repository.UserRepository;
import com.siupo.restaurant.security.JwtUtils;
import com.siupo.restaurant.service.mail.EmailService;
import jakarta.mail.MessagingException;
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

    // Danh sách user đang chờ xác thực OTP
    private final Map<String, PendingUser> pendingUsers = new ConcurrentHashMap<>();
    // Cấu trúc lưu user chờ xác nhận
    @Getter
    @RequiredArgsConstructor
    private static class PendingUser {
        private final RegisterRequest registerRequest;
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

        PendingUser existing = pendingUsers.get(registerRequest.getEmail());
        if (existing != null && !existing.isExpired() && existing.attempts()) {
            return new MessageDataReponse(true,"200","Vui lòng kiểm tra email, mã OTP vẫn còn hiệu lực!");
        }

        String otp = generateOTP();
        String otpHash = passwordEncoder.encode(otp);

        pendingUsers.put(registerRequest.getEmail(),
                new PendingUser(registerRequest, otpHash, Instant.now().plusSeconds(300)));

        try {
            if(emailService.sendOTPToEmail(registerRequest.getEmail(), otp))
                return new MessageDataReponse(true,"201","Đã gửi mã OTP tới email!");
            else {
                pendingUsers.remove(registerRequest.getEmail());
                return new MessageDataReponse(false,"400","Không thể gửi email OTP, vui lòng thử lại!");
            }

        } catch (MessagingException e) {
            pendingUsers.remove(registerRequest.getEmail());
            return new MessageDataReponse(false,"400","Không thể gửi email OTP, vui lòng thử lại!");
        }
    }

    // =============== XÁC NHẬN OTP ===============
    @Override
    public MessageDataReponse confirmRegistration(String email, String otp) {
        PendingUser pendingUser = pendingUsers.get(email);

        if (pendingUser == null || pendingUser.isExpired()) {
            pendingUsers.remove(email);
            return new MessageDataReponse(false,"400","Yêu cầu đăng ký không tồn tại hoặc đã hết hạn!");
        }


        if (pendingUser.attempts <= 0) {
            pendingUsers.remove(email);
            return new MessageDataReponse(false,"400","Bạn đã nhập sai OTP quá 5 lần, vui lòng đăng ký lại!");
        }

        if (!passwordEncoder.matches(otp, pendingUser.getOtpHash())) {
            pendingUser.attempts--;
            Map<String, Object> data = new HashMap<>();
            data.put("attempt", pendingUser.attempts);
            data.put("message", "Bạn còn lại " + pendingUser.attempts + " lượt");
            return new MessageDataReponse(false,"400","OTP không đúng!",data);
        }

        RegisterRequest req = pendingUser.getRegisterRequest();
        User newUser = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        userRepository.save(newUser);
        pendingUsers.remove(email);
        return new MessageDataReponse(true,"200","Xác thực thành công! Tài khoản đã được tạo.");
    }

    // =============== GỬI LẠI OTP ===============
    @Override
    public void resendOtp(String email) {
        PendingUser pendingUser = pendingUsers.get(email);

        if (pendingUser == null)
            throw new BadRequestException("Không tìm thấy yêu cầu đăng ký nào cho email này!");

        // Tạo OTP mới
        String newOtp = generateOTP();
        String newOtpHash = passwordEncoder.encode(newOtp);
        Instant newExpiry = Instant.now().plusSeconds(300);

        pendingUsers.put(email, new PendingUser(pendingUser.getRegisterRequest(), newOtpHash, newExpiry));

        try {
            emailService.sendOTPToEmail(email, newOtp);
        } catch (MessagingException e) {
            throw new BadRequestException("Gửi lại email OTP thất bại!");
        }
    }

    // =============== DỌN OTP HẾT HẠN ===============
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredPendingUsers() {
        pendingUsers.entrySet().removeIf(entry -> entry.getValue().isExpired());
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
                    .build();
        }
        User user = userOpt.get();

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return LoginDataResponse.builder()
                    .message("Đăng nhập thất bại: Mât khẩu không đúng")
                    .accessToken(null)
                    .build();
        }

        // 3. Revoke các refresh token cũ
        List<RefreshToken> existingTokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        existingTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAllAndFlush(existingTokens); // flush ngay tránh khóa DB

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

        // 6. Trả response chuẩn
        return LoginDataResponse.builder()
                .message("Đăng nhập thành công")
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
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
}