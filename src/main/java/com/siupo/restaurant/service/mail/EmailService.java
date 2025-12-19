package com.siupo.restaurant.service.mail;

import jakarta.mail.MessagingException;

public interface EmailService {
    boolean sendOTPToEmail(String toEmail, String otp, String subject) throws MessagingException;
}
