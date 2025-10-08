package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.LoginRequestDTO;
import com.siupo.restaurant.dto.request.LogoutRequestDTO;
import com.siupo.restaurant.dto.request.RefreshTokenRequestDTO;
import com.siupo.restaurant.dto.request.RegisterRequestDTO;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.AuthResponseDTO;
import com.siupo.restaurant.service.authentication.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authenticationService.login(request);
        ApiResponse<AuthResponseDTO> apiResponse = ApiResponse.<AuthResponseDTO>builder()
                .success(true)
                .message("Đăng nhập thành công!")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequestDTO user) {
        authenticationService.register(user);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
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
                .message("Xác thực thành công! Tài khoản đã được tạo.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resend(@RequestParam String email) {
        authenticationService.resendOtp(email);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Đã gửi lại mã OTP mới!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO authResponse = authenticationService.refreshToken(request);

        ApiResponse<AuthResponseDTO> response = ApiResponse.<AuthResponseDTO>builder()
                .success(true)
                .message("Refresh token thành công")
                .data(authResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequestDTO request) {
        authenticationService.logout(request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Đăng xuất thành công!")
                .build();
        return ResponseEntity.ok(response);
    }
}
