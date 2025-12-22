package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.*;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.LoginResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.service.authentication.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    
    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.refresh-cookie-name}")
    private String refreshTokenCookieName;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .success(true)
                .code("200")
                .message("Login successful")
                .data(loginResponse)
                .build();
        ResponseCookie refreshTokenCookie = buildRefreshCookie(loginResponse.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest user) {
        authenticationService.register(user);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("201")
                .message("Registration successful! Please check your email to confirm your account.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-forgot-password")
    public ResponseEntity<ApiResponse<Void>> requestForgotPassword(@RequestParam String email) {
        authenticationService.requestForgotPassword(email);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("If the email is registered, a password reset OTP has been sent.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-new-password")
    public ResponseEntity<ApiResponse<Void>> setNewPassWord(@Valid @RequestBody ConfirmForgotPasswordRequest request) {
        authenticationService.setNewPassword(request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Password has been reset successfully!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(@Valid @RequestBody ConfirmRegistrationRequest request) {
        authenticationService.confirmRegistration(request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Account confirmed successfully!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resend(@RequestParam String email) {
        authenticationService.resendOtp(email);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("OTP has been resent successfully!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        LoginResponse authResponse = authenticationService.refreshToken(refreshToken);
        ResponseCookie refreshCookie = buildRefreshCookie(authResponse.getRefreshToken());
        LoginResponse responseData = LoginResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .build();
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .success(true)
                .code("200")
                .message("Refresh token successful")
                .data(responseData)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authenticationService.logout(refreshToken);
        }
        ResponseCookie clearCookie = ResponseCookie.from(refreshTokenCookieName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .path("/")
                .build();
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Logout successful")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(response);
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> refreshTokenCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenCookieName, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(refreshTokenExpiration / 1000)
                .path("/")
                .build();
    }
}
