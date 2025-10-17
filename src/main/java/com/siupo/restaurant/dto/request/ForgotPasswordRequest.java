package com.siupo.restaurant.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
    private String newPassword;
    private String otp;
}