package com.siupo.restaurant.service.otp;

import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.model.OtpRedis;
import com.siupo.restaurant.repository.OtpRedisRepository;
import com.siupo.restaurant.service.mail.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpRedisRepository otpRedisRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void generateAndSendOtp(String email, String subject) {
        String otp = generateRandomOtp();
        String hashedOtp = passwordEncoder.encode(otp);
        OtpRedis entity = new OtpRedis(email, hashedOtp);
        otpRedisRepository.save(entity);
        try {
            emailService.sendOTPToEmail(email, otp, subject);
        } catch (MessagingException e) {
            throw new BadRequestException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }

    @Override
    public void verifyOtp(String email, String inputOtp) {
        OtpRedis entity = otpRedisRepository.findById(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.OTP_NOT_FOUND_OR_EXPIRED));
        if (!passwordEncoder.matches(inputOtp, entity.getOtp())) {
            throw new BadRequestException(ErrorCode.OTP_INVALID);
        }
        otpRedisRepository.delete(entity);
    }

    private String generateRandomOtp() {
        return String.valueOf(new SecureRandom().nextInt(900000) + 100000);
    }
}
