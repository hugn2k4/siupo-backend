package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.LoginRequest;
import com.siupo.restaurant.dto.request.RegisterRequest;
import com.siupo.restaurant.dto.request.RefreshTokenRequest;
import com.siupo.restaurant.dto.request.LogoutRequest;
import com.siupo.restaurant.dto.response.AuthResponse;
import com.siupo.restaurant.exception.BadRequestException;
import com.siupo.restaurant.exception.NotFoundException;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        private int attempts = 0;

        public boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }
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
    public void register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent())
            throw new BadRequestException("Email đã tồn tại!");

        PendingUser existing = pendingUsers.get(registerRequest.getEmail());
        if (existing != null && !existing.isExpired()) {
            throw new BadRequestException("Vui lòng kiểm tra email, mã OTP vẫn còn hiệu lực!");
        }

        String otp = generateOTP();
        String otpHash = passwordEncoder.encode(otp);

        pendingUsers.put(registerRequest.getEmail(),
                new PendingUser(registerRequest, otpHash, Instant.now().plusSeconds(300)));

        try {
            emailService.sendOTPToEmail(registerRequest.getEmail(), otp);
        } catch (MessagingException e) {
            pendingUsers.remove(registerRequest.getEmail());
            throw new BadRequestException("Không thể gửi email OTP, vui lòng thử lại!");
        }
    }

    // =============== XÁC NHẬN OTP ===============
    @Override
    public void confirmRegistration(String email, String otp) {
        PendingUser pendingUser = pendingUsers.get(email);

        if (pendingUser == null || pendingUser.isExpired()) {
            pendingUsers.remove(email);
            throw new BadRequestException("Yêu cầu đăng ký không tồn tại hoặc đã hết hạn!");
        }

        if (pendingUser.attempts >= 5) {
            pendingUsers.remove(email);
            throw new BadRequestException("Bạn đã nhập sai OTP quá 5 lần, vui lòng đăng ký lại!");
        }

        if (!passwordEncoder.matches(otp, pendingUser.getOtpHash())) {
            pendingUser.attempts++;
            throw new BadRequestException("OTP không đúng!");
        }

        RegisterRequest req = pendingUser.getRegisterRequest();
        User newUser = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        userRepository.save(newUser);
        pendingUsers.remove(email);
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
    public AuthResponse login(LoginRequest loginRequest) {
        // 1. Lấy user theo email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại!"));

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Mật khẩu không đúng!");
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
        return AuthResponse.builder()
                .message("Đăng nhập thành công")
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
    }

    // =============== REFRESH TOKEN ===============
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
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
        
        return AuthResponse.builder()
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