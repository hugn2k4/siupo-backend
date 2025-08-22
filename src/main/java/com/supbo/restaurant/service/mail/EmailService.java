package com.supbo.restaurant.service.mail;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendOTPToEmail(String toEmail, String otp) throws MessagingException;
}
