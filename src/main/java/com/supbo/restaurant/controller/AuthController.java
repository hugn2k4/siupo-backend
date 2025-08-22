package com.supbo.restaurant.controller;

import com.supbo.restaurant.dto.request.UserLoginRequestDTO;
import com.supbo.restaurant.dto.request.UserRegisterRequestDTO;
import com.supbo.restaurant.dto.response.UserRegisterResponseDTO;
import com.supbo.restaurant.service.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequestDTO user) {
        try {
            authenticationService.register(user);
            return ResponseEntity.ok(Map.of(
                    "message", "OTP đã được gửi tới email."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/confirm-registration")
    public ResponseEntity<?> confirmRegistration(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String otp = body.get("otp");

            authenticationService.confirmRegistration(email, otp);

            return ResponseEntity.ok(Map.of(
                    "message", "Xác thực thành công! Tài khoản đã được tạo."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDTO user) {
        try {
            return ResponseEntity.ok(authenticationService.login(user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }
}
