package com.smartcampus.auth.service.impl;

import com.smartcampus.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.enabled:false}")
    private boolean sendGridEnabled;

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

    @Override
    @Async
    public void sendNotificationEmail(String to, String name, String subject, String title, String message,
            String category) {
        String content = buildNotificationEmailContent(name, title, message, category);
        sendHtmlEmail(to, subject, content);
        log.info("Notification email sent to: {} [category: {}]", to, category);
    }

    private void sendHtmlEmail(String to, String subject, String content) {
        if (sendGridEnabled && sendGridApiKey != null && !sendGridApiKey.isEmpty()) {
            sendViaSendGridHttpApi(to, subject, content);
        } else {
            log.warn("SendGrid HTTP API is not enabled or API key is missing. Email sending is disabled.");
            // Email gönderilemese bile exception fırlatma, sadece log'la
        }
    }

    private void sendViaSendGridHttpApi(String to, String subject, String content) {
        try {
            Map<String, Object> personalization = new HashMap<>();
            personalization.put("to", List.of(Map.of("email", to)));

            // From email with name (spam filtrelerini iyileştirir)
            String senderEmail = fromEmail != null && !fromEmail.isEmpty() ? fromEmail : "noreply@smartcampus.edu.tr";
            Map<String, Object> from = new HashMap<>();
            from.put("email", senderEmail);
            from.put("name", "Smart Campus"); // Gönderen ismi ekle

            // Reply-To header (profesyonel görünüm)
            Map<String, Object> replyTo = new HashMap<>();
            replyTo.put("email", senderEmail);
            replyTo.put("name", "Smart Campus Support");

            // HTML ve Plain Text içerik (multipart email spam'e düşme riskini azaltır)
            Map<String, Object> htmlContent = new HashMap<>();
            htmlContent.put("type", "text/html");
            htmlContent.put("value", content);

            // Plain text versiyonu (HTML'den basit dönüşüm)
            String plainText = content
                    .replaceAll("<style[^>]*>[^<]*</style>", "")
                    .replaceAll("<[^>]+>", "")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text/plain");
            textContent.put("value", plainText);

            // Tracking ayarları (tıklama takibi kapatılabilir - bazen spam'e neden olur)
            Map<String, Object> trackingSettings = new HashMap<>();
            trackingSettings.put("click_tracking", Map.of("enable", false, "enable_text", false));
            trackingSettings.put("open_tracking", Map.of("enable", true));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("personalizations", List.of(personalization));
            requestBody.put("from", from);
            requestBody.put("reply_to", replyTo);
            requestBody.put("subject", subject);
            requestBody.put("content", List.of(textContent, htmlContent)); // Plain text önce, HTML sonra
            requestBody.put("tracking_settings", trackingSettings);

            WebClient webClient = webClientBuilder
                    .baseUrl("https://api.sendgrid.com")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + sendGridApiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            webClient.post()
                    .uri("/v3/mail/send")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("SendGrid API error response (Status: {}): {}",
                                            clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("SendGrid API error: " + errorBody));
                                });
                    })
                    .bodyToMono(String.class)
                    .block();

            log.info("Email sent successfully via SendGrid HTTP API to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email via SendGrid HTTP API to {}: {}", to, e.getMessage(), e);
            // Email gönderme hatası uygulamayı durdurmamalı
            // Sadece log'layalım, exception fırlatmayalım
            log.warn("Email gönderilemedi ama kullanıcı kaydı tamamlandı. Email: {}", to);
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
                """
                .formatted(name, verificationLink, verificationLink);
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
                """
                .formatted(name, resetLink, resetLink);
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
                """
                .formatted(name, frontendUrl);
    }

    private String buildNotificationEmailContent(String name, String title, String message, String category) {
        // Kategoriye göre renk belirleme
        String color = switch (category.toUpperCase()) {
            case "ACADEMIC" -> "#4F46E5"; // Mavi
            case "ATTENDANCE" -> "#059669"; // Yeşil
            case "MEAL" -> "#F59E0B"; // Turuncu
            case "EVENT" -> "#8B5CF6"; // Mor
            case "PAYMENT" -> "#EF4444"; // Kırmızı
            case "SYSTEM" -> "#6B7280"; // Gri
            default -> "#4F46E5";
        };

        String emoji = switch (category.toUpperCase()) {
            case "ACADEMIC" -> "📚";
            case "ATTENDANCE" -> "📍";
            case "MEAL" -> "🍽️";
            case "EVENT" -> "📅";
            case "PAYMENT" -> "💳";
            case "SYSTEM" -> "⚙️";
            default -> "🔔";
        };

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: %s; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                        .notification-box { background: white; padding: 20px; border-radius: 8px; border-left: 4px solid %s; margin: 15px 0; }
                        .button { display: inline-block; background: %s; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                        .category-badge { display: inline-block; background: %s; color: white; padding: 4px 12px; border-radius: 12px; font-size: 12px; margin-bottom: 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s Smart Campus Bildirimi</h1>
                        </div>
                        <div class="content">
                            <h2>Merhaba %s,</h2>
                            <div class="notification-box">
                                <span class="category-badge">%s</span>
                                <h3>%s</h3>
                                <p>%s</p>
                            </div>
                            <p style="text-align: center;">
                                <a href="%s/notifications" class="button">Bildirimleri Görüntüle</a>
                            </p>
                            <p style="color: #666; font-size: 12px;">
                                Bu bildirimi almak istemiyorsanız, hesap ayarlarınızdan bildirim tercihlerinizi güncelleyebilirsiniz.
                            </p>
                        </div>
                        <div class="footer">
                            <p>© 2025 Smart Campus. Tüm hakları saklıdır.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(color, color, color, color, emoji, name, category, title, message, frontendUrl);
    }
}
