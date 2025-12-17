package com.siupo.restaurant.service.otp;

public interface OtpService {
    void generateAndSendOtp(String email, String otpCode);
    void verifyOtp(String email, String otpCode);
}
