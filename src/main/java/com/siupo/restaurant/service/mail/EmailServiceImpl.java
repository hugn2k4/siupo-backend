package com.siupo.restaurant.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean sendOTPToEmail(String toEmail, String otp) throws MessagingException {
        try {
            log.info("🔄 Preparing to send OTP email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận tài khoản của bạn");

            String htmlContent = getHtmlContent(otp);
            helper.setText(htmlContent, true);

            log.info("📧 Sending OTP email to: {}", toEmail);
            mailSender.send(message);
            log.info("✅ OTP email sent successfully to: {}", toEmail);

            return true;
        } catch (MessagingException e) {
            log.error("❌ Failed to send OTP email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw e;
        }
    }

    private String getHtmlContent(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        background-color: #f0f4f8; /* nền tổng thể nhẹ nhàng */
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        margin: 0;
                        padding: 0;
                    }
                    .email-wrapper {
                        max-width: 700px;
                        margin: 50px auto;
                        padding: 20px;
                    }
                    .email-container {
                        background: linear-gradient(326deg, #86ffe799, #d6aeffd4);
                        border-radius: 14px;
                        padding: 40px;
                        box-shadow: 0 10px 25px rgba(0,0,0,0.08);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 30px;
                        color: #1a1a1a; /* chữ đen hiện đại */
                    }
                    .intro {
                        font-size: 16px;
                        color: #333333;
                        line-height: 1.6;
                        margin-bottom: 25px;
                    }
                    .otp-container {
                        text-align: center;
                        margin: 30px 0;
                    }
                    .otp-code {
                        display: inline-block;
                        font-size: 38px;
                        font-weight: bold;
                        color: #ff6f20; /* cam rực rỡ */
                        background: #fff7f0; /* nền nhẹ cho OTP */
                        padding: 20px 50px;
                        border-radius: 12px;
                        letter-spacing: 5px;
                        box-shadow: 0 4px 15px rgba(0,0,0,0.1);
                    }
                    .instructions {
                        font-size: 15px;
                        color: #555555;
                        line-height: 1.6;
                        margin-top: 20px;
                        text-align: center;
                    }
                    .button-container {
                        text-align: center;
                        margin-top: 35px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 45px;
                        font-size: 13px;
                        color: #888888;
                        line-height: 1.5;
                    }
                </style>
                </head>
                <body>
                <div class="email-wrapper">
                    <div class="email-container">
                        <div class="header">
                            <h1>Mã xác nhận OTP</h1>
                        </div>
                        <div class="intro">
                            Xin chào,<br/><br/>
                            Chúng tôi đã nhận được yêu cầu của bạn.\s
                            Vui lòng sử dụng mã OTP dưới đây để hoàn tất quá trình xác thực.\s
                            Mã này chỉ có hiệu lực trong 1 phút và không được chia sẻ với bất kỳ ai.
                        </div>
                        <div class="otp-container">
                            <div class="otp-code">%s</div>
                        </div>
                        <div class="instructions">
                            Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.<br/>
                            Để bảo mật tài khoản, hãy không chia sẻ mã OTP với bất kỳ ai.
                        </div>
                        <div class="footer">
                            Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.<br/>
                            Nếu bạn có thắc mắc, vui lòng liên hệ bộ phận hỗ trợ.
                        </div>
                    </div>
                </div>
                </body>
                </html>
        """.formatted(otp);
    }
}
