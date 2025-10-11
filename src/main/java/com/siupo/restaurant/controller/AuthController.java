package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.LoginRequest;
import com.siupo.restaurant.dto.request.LogoutRequest;
import com.siupo.restaurant.dto.request.RefreshTokenRequest;
import com.siupo.restaurant.dto.request.RegisterRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.LoginDataResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;
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
    public ResponseEntity<ApiResponse<LoginDataResponse>> login(@RequestBody LoginRequest request) {
        LoginDataResponse dataResponse = authenticationService.login(request);
        if (dataResponse.getAccessToken() == null) {
            ApiResponse<LoginDataResponse> errorResponse = ApiResponse.<LoginDataResponse>builder()
                    .success(false)
                    .code("401")
                    .message(dataResponse.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.ok()
                    .body(errorResponse);
        }
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, dataResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true) // Chỉ gửi qua HTTPS (set false cho development)
                .sameSite("Strict")
                .maxAge(refreshTokenExpiration / 1000)
                .path("/")
                .build();

        ApiResponse<LoginDataResponse> apiResponse = ApiResponse.<LoginDataResponse>builder()
                .success(true)
                .code("200")
                .message("Đăng nhập thành công!")
                .data(dataResponse)
                .build();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest user) {
        MessageDataReponse messageDataReponse = authenticationService.register(user);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(messageDataReponse.isSuccess())
                .code(messageDataReponse.getCode())
                .message(messageDataReponse.getMessage())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        MessageDataReponse dataReponse = authenticationService.confirmRegistration(email, otp);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(dataReponse.isSuccess())
                .code(dataReponse.getCode())
                .message(dataReponse.getMessage())
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

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginDataResponse>> refreshToken(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            ApiResponse<LoginDataResponse> errorResponse = ApiResponse.<LoginDataResponse>builder()
                    .success(false)
                    .code("401")
                    .message("Refresh token không tồn tại")
                    .build();
            return ResponseEntity.status(401).body(errorResponse);
        }
        
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);
        LoginDataResponse authResponse = authenticationService.refreshToken(refreshRequest);

        ResponseCookie newRefreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(refreshTokenExpiration / 1000)
                .path("/")
                .build();

        LoginDataResponse secureResponse = LoginDataResponse.builder()
                .message(authResponse.getMessage())
                .accessToken(authResponse.getAccessToken())
                .build();

        ApiResponse<LoginDataResponse> response = ApiResponse.<LoginDataResponse>builder()
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
