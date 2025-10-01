package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.UserLoginRequestDTO;
import com.siupo.restaurant.dto.request.UserRegisterRequestDTO;
import com.siupo.restaurant.dto.response.UserLoginResponseDTO;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.UserRepository;
import com.siupo.restaurant.security.JwtUtils;
import com.siupo.restaurant.service.mail.EmailService;
import jakarta.mail.MessagingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final Map<String, PendingUser> pendingUsers = new ConcurrentHashMap<>();

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     JwtUtils jwtUtils,
                                     BCryptPasswordEncoder passwordEncoder,
                                     EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public void register(UserRegisterRequestDTO registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent())
            throw new com.siupo.restaurant.exception.BadRequestException("Username đã tồn tại!");
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent())
            throw new com.siupo.restaurant.exception.BadRequestException("Email đã tồn tại!");

        String otp = generateOTP();
        pendingUsers.put(registerRequest.getEmail(), new PendingUser(registerRequest, otp, Instant.now().plusSeconds(300)));

        try {
            emailService.sendOTPToEmail(registerRequest.getEmail(), otp);
        } catch (MessagingException e) {
            throw new com.siupo.restaurant.exception.BadRequestException("Gửi email OTP thất bại!");
        }
    }

    @Override
    public void confirmRegistration(String email, String otp) {
        PendingUser pendingUser = pendingUsers.get(email);
        if (pendingUser == null || pendingUser.isExpired())
            throw new com.siupo.restaurant.exception.BadRequestException("Yêu cầu đăng ký không tồn tại hoặc đã hết hạn!");
        if (!pendingUser.getOtp().equals(otp))
            throw new com.siupo.restaurant.exception.BadRequestException("OTP không đúng!");

        UserRegisterRequestDTO req = pendingUser.getRegisterRequest();
        User newUser = new User();
        newUser.setFullName(req.getFullName());
        newUser.setUsername(req.getUsername());
        newUser.setEmail(req.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        userRepository.save(newUser);
        pendingUsers.remove(email);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequestDTO loginRequest) {
    User user = userRepository.findByUsername(loginRequest.getUsername())
        .orElseThrow(() -> new com.siupo.restaurant.exception.NotFoundException("Tài khoản không tồn tại!"));

    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash()))
        throw new com.siupo.restaurant.exception.UnauthorizedException("Password không đúng!");

    String token = jwtUtils.generateJwtToken(user.getUsername());
    return new UserLoginResponseDTO(  token,
        user.getId(),
        user.getUsername(),
        user.getEmail());
    }

    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    @Getter
    @RequiredArgsConstructor
    private static class PendingUser {
        private final UserRegisterRequestDTO registerRequest;
        private final String otp;
        private final Instant expiryTime;
        public boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }


    }
}
