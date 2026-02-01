package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.*;
import com.siupo.restaurant.dto.response.LoginResponse;
import com.siupo.restaurant.enums.EUserStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.ConflictException;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.exception.business.UnauthorizedException;
import com.siupo.restaurant.mapper.UserMapper;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.PendingRegistrationRepository;
import com.siupo.restaurant.repository.UserRepository;
import com.siupo.restaurant.service.otp.OtpService;
import com.siupo.restaurant.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;
    private final OtpService otpService;
    private final PendingRegistrationRepository pendingRegistrationRepository;

    private final UserMapper userMapper;

    // =============== ĐĂNG NHẬP ===============
    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
        }
        LoginResponse response = tokenService.generateAuthResponse(user);
        response.setUser(userMapper.toDto(user));
        return response;
    }

    // =============== ĐĂNG KÝ ===============
    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS);
        // 1. Lưu thông tin đăng ký tạm vào Redis (để dùng lại khi confirm)
        PendingRegistrationRedis pendingUser = new PendingRegistrationRedis(
                request.getEmail(),
                request.getFullName(),
                request.getPhoneNumber(),
                passwordEncoder.encode(request.getPassword())
        );
        pendingRegistrationRepository.save(pendingUser);

        // 2. Sinh và gửi OTP
        otpService.generateAndSendOtp(request.getEmail(), "Xác thực đăng ký tài khoản");
    }

    // =============== XÁC NHẬN OTP ===============
    @Override
    public void confirmRegistration(ConfirmRegistrationRequest request) {
        // 1. Verify OTP (Nếu sai sẽ ném Exception ngay tại đây)
        otpService.verifyOtp(request.getEmail(), request.getOtp());

        // 2. Lấy thông tin đăng ký tạm từ Redis
        PendingRegistrationRedis pendingUser = pendingRegistrationRepository.findById(request.getEmail())
                .orElseThrow(() -> new BadRequestException(ErrorCode.REGISTRATION_NOT_FOUND_OR_EXPIRED));

        // 3. Lưu vào MySQL (Chính thức tạo user)
        Customer newUser = Customer.builder()
                .email(pendingUser.getEmail())
                .fullName(pendingUser.getFullName())
                .password(pendingUser.getPassword())
                .phoneNumber(pendingUser.getPhoneNumber())
                .status(EUserStatus.ACTIVE)
                .totalSpent(0.0)
                .build();
        userRepository.save(newUser);

        // 4. Dọn dẹp Redis
        pendingRegistrationRepository.delete(pendingUser);
    }

    // =============== QUÊN MẬT KHẨU ===============
    @Override
    public void requestForgotPassword(String email) {
        userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        otpService.generateAndSendOtp(email, "Mã xác thực quên mật khẩu");
    }

    @Override
    public void setNewPassword(ConfirmForgotPasswordRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtp());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // =============== CÁC HÀM KHÁC ===============
    @Override
    public void resendOtp(String email) {
        otpService.generateAndSendOtp(email, "Gửi lại mã OTP");
    }

    @Override
    public LoginResponse refreshToken(String token) {
        return tokenService.refreshAccessToken(token);
    }

    @Override
    public void logout(String token) {
        tokenService.revokeToken(token);
    }

    @Override
    @Transactional
    public User processOAuth2User(String email, String name, String picture) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) return existingUser.get();

        Customer newCustomer = Customer.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .fullName(name != null ? name : "Google User")
                .status(EUserStatus.ACTIVE)
                .totalSpent(0.0)
                .build();
        return userRepository.save(newCustomer);
    }
}