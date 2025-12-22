package com.siupo.restaurant.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfirmForgotPasswordRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 32, message = "Mật khẩu phải từ 8–32 ký tự")
    private String newPassword;

    @NotBlank(message = "OTP không được để trống")
    @Pattern(
            regexp = "\\d{6}",
            message = "OTP phải gồm 6 chữ số"
    )
    private String otp;
}