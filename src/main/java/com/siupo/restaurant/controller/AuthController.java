package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.LoginRequest;
import com.siupo.restaurant.dto.request.LogoutRequest;
import com.siupo.restaurant.dto.request.RefreshTokenRequest;
import com.siupo.restaurant.dto.request.RegisterRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.AuthResponse;
import com.siupo.restaurant.service.authentication.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    
    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;
    
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, response.getRefreshToken())
                .httpOnly(true)
                .secure(true) // Chỉ gửi qua HTTPS (set false cho development)
                .sameSite("Strict")
                .maxAge(refreshTokenExpiration / 1000)
                .path("/")
                .build();

        AuthResponse secureResponse = AuthResponse.builder()
                .message(response.getMessage())
                .accessToken(response.getAccessToken())
                .build();
        
        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
                .success(true)
                .code("200")
                .message("Đăng nhập thành công!")
                .data(secureResponse)
                .build();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest user) {
        authenticationService.register(user);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("OTP đã được gửi tới email.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        authenticationService.confirmRegistration(email, otp);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Xác thực thành công! Tài khoản đã được tạo.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resend(@RequestParam String email) {
        authenticationService.resendOtp(email);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Đã gửi lại mã OTP mới!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            ApiResponse<AuthResponse> errorResponse = ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .code("401")
                    .message("Refresh token không tồn tại")
                    .build();
            return ResponseEntity.status(401).body(errorResponse);
        }
        
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);
        AuthResponse authResponse = authenticationService.refreshToken(refreshRequest);

        ResponseCookie newRefreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(refreshTokenExpiration / 1000)
                .path("/")
                .build();

        AuthResponse secureResponse = AuthResponse.builder()
                .message(authResponse.getMessage())
                .accessToken(authResponse.getAccessToken())
                .build();

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .success(true)
                .code("200")
                .message("Refresh token thành công")
                .data(secureResponse)
                .build();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        // Lấy refresh token từ cookie
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            LogoutRequest logoutRequest = new LogoutRequest();
            logoutRequest.setRefreshToken(refreshToken);
            authenticationService.logout(logoutRequest);
        }
        
        // Xóa refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0) // Xóa cookie
                .path("/")
                .build();

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Đăng xuất thành công!")
                .build();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(response);
    }
    
    /**
     * Helper method để lấy refresh token từ cookie
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
