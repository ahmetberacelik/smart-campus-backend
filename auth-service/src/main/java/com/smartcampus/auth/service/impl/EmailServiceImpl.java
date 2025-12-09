package com.smartcampus.auth.service.impl;

import com.smartcampus.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        String subject = "Smart Campus - Email Doğrulama";
        String verificationLink = frontendUrl + "/verify-email?token=" + token;

        String content = buildVerificationEmailContent(name, verificationLink);

        sendHtmlEmail(to, subject, content);
        log.info("Verification email sent to: {}", to);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        String subject = "Smart Campus - Şifre Sıfırlama";
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String content = buildPasswordResetEmailContent(name, resetLink);

        sendHtmlEmail(to, subject, content);
        log.info("Password reset email sent to: {}", to);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Smart Campus'a Hoş Geldiniz!";

        String content = buildWelcomeEmailContent(name);

        sendHtmlEmail(to, subject, content);
        log.info("Welcome email sent to: {}", to);
    }

    private void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email gönderilemedi", e);
        }
    }

    private String buildVerificationEmailContent(String name, String verificationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 Smart Campus</h1>
                    </div>
                    <div class="content">
                        <h2>Merhaba %s,</h2>
                        <p>Smart Campus'a hoş geldiniz! Hesabınızı aktifleştirmek için lütfen aşağıdaki butona tıklayın:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Email Adresimi Doğrula</a>
                        </p>
                        <p>Veya aşağıdaki linki tarayıcınıza yapıştırın:</p>
                        <p style="word-break: break-all; color: #4F46E5;">%s</p>
                        <p><strong>Bu link 24 saat geçerlidir.</strong></p>
                        <p>Eğer bu hesabı siz oluşturmadıysanız, bu emaili görmezden gelebilirsiniz.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Smart Campus. Tüm hakları saklıdır.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, verificationLink, verificationLink);
    }

    private String buildPasswordResetEmailContent(String name, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #DC2626; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #DC2626; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .warning { background: #FEF2F2; border-left: 4px solid #DC2626; padding: 10px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Şifre Sıfırlama</h1>
                    </div>
                    <div class="content">
                        <h2>Merhaba %s,</h2>
                        <p>Şifrenizi sıfırlamak için bir talep aldık. Şifrenizi sıfırlamak için aşağıdaki butona tıklayın:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Şifremi Sıfırla</a>
                        </p>
                        <p>Veya aşağıdaki linki tarayıcınıza yapıştırın:</p>
                        <p style="word-break: break-all; color: #DC2626;">%s</p>
                        <div class="warning">
                            <strong>⚠️ Önemli:</strong> Bu link 1 saat geçerlidir.
                        </div>
                        <p>Eğer bu talebi siz yapmadıysanız, bu emaili görmezden gelebilirsiniz. Şifreniz değişmeyecektir.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Smart Campus. Tüm hakları saklıdır.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, resetLink, resetLink);
    }

    private String buildWelcomeEmailContent(String name) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #059669; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #059669; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .feature { background: white; padding: 15px; margin: 10px 0; border-radius: 6px; border-left: 4px solid #059669; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Hoş Geldiniz!</h1>
                    </div>
                    <div class="content">
                        <h2>Merhaba %s,</h2>
                        <p>Email adresiniz başarıyla doğrulandı! Artık Smart Campus'un tüm özelliklerini kullanabilirsiniz.</p>
                        
                        <h3>Neler yapabilirsiniz?</h3>
                        <div class="feature">📚 Ders kaydı ve yönetimi</div>
                        <div class="feature">📍 GPS ile yoklama</div>
                        <div class="feature">🍽️ Yemek rezervasyonu</div>
                        <div class="feature">📅 Etkinliklere katılım</div>
                        <div class="feature">📊 Not takibi</div>
                        
                        <p style="text-align: center;">
                            <a href="%s" class="button">Smart Campus'a Git</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Smart Campus. Tüm hakları saklıdır.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, frontendUrl);
    }
}

