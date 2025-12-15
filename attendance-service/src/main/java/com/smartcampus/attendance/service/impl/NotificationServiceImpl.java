package com.smartcampus.attendance.service.impl;

import com.smartcampus.attendance.service.NotificationService;
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
public class NotificationServiceImpl implements NotificationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.enabled:false}")
    private boolean sendGridEnabled;

    @Value("${sendgrid.from-email:noreply@smartcampus.edu.tr}")
    private String fromEmail;

    @Override
    @Async
    public void sendAbsenceWarningEmail(String to, String studentName, String courseCode,
            String courseName, int absentCount, int totalSessions, double absencePercent) {
        String subject = "Smart Campus - Devamsızlık Uyarısı: " + courseCode;
        String content = buildWarningEmailContent(studentName, courseCode, courseName, 
                absentCount, totalSessions, absencePercent);
        sendHtmlEmail(to, subject, content);
        log.info("Absence warning email sent to: {} for course: {}", to, courseCode);
    }

    @Override
    @Async
    public void sendCriticalAbsenceEmail(String to, String studentName, String courseCode,
            String courseName, int absentCount, int totalSessions, double absencePercent) {
        String subject = "Smart Campus - KRİTİK: Ders Kalma Riski: " + courseCode;
        String content = buildCriticalEmailContent(studentName, courseCode, courseName,
                absentCount, totalSessions, absencePercent);
        sendHtmlEmail(to, subject, content);
        log.info("Critical absence email sent to: {} for course: {}", to, courseCode);
    }

    private void sendHtmlEmail(String to, String subject, String content) {
        if (!sendGridEnabled || sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            log.warn("SendGrid is not enabled. Email not sent to: {}", to);
            return;
        }

        try {
            Map<String, Object> personalization = new HashMap<>();
            personalization.put("to", List.of(Map.of("email", to)));

            Map<String, Object> from = new HashMap<>();
            from.put("email", fromEmail);
            from.put("name", "Smart Campus");

            Map<String, Object> htmlContent = new HashMap<>();
            htmlContent.put("type", "text/html");
            htmlContent.put("value", content);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("personalizations", List.of(personalization));
            requestBody.put("from", from);
            requestBody.put("subject", subject);
            requestBody.put("content", List.of(htmlContent));

            WebClient webClient = webClientBuilder
                    .baseUrl("https://api.sendgrid.com")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + sendGridApiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            webClient.post()
                    .uri("/v3/mail/send")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> 
                        clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("SendGrid API error: {}", errorBody);
                                return Mono.error(new RuntimeException("SendGrid API error"));
                            }))
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildWarningEmailContent(String name, String courseCode, String courseName,
            int absentCount, int totalSessions, double absencePercent) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #F59E0B; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .stats { background: white; padding: 15px; margin: 15px 0; border-radius: 6px; border-left: 4px solid #F59E0B; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Devamsızlık Uyarısı</h1>
                    </div>
                    <div class="content">
                        <h2>Merhaba %s,</h2>
                        <p><strong>%s - %s</strong> dersindeki devamsızlık oranınız uyarı seviyesine ulaştı.</p>
                        
                        <div class="stats">
                            <p><strong>Ders:</strong> %s - %s</p>
                            <p><strong>Devamsız Olduğunuz Ders:</strong> %d / %d</p>
                            <p><strong>Devamsızlık Oranı:</strong> %%.1f%%</p>
                        </div>
                        
                        <p>Lütfen devamsızlık limitini aşmamaya dikkat ediniz. Mazeretli devamsızlık için mazeret belgesi sunabilirsiniz.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Smart Campus. Tüm hakları saklıdır.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, courseCode, courseName, courseCode, courseName, absentCount, totalSessions, absencePercent);
    }

    private String buildCriticalEmailContent(String name, String courseCode, String courseName,
            int absentCount, int totalSessions, double absencePercent) {
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
                    .stats { background: #FEE2E2; padding: 15px; margin: 15px 0; border-radius: 6px; border-left: 4px solid #DC2626; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .warning { font-size: 18px; color: #DC2626; font-weight: bold; text-align: center; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚨 KRİTİK DEVAMSIZLIK</h1>
                    </div>
                    <div class="content">
                        <h2>Merhaba %s,</h2>
                        
                        <div class="warning">DERS KALMA RİSKİ!</div>
                        
                        <p><strong>%s - %s</strong> dersindeki devamsızlık oranınız kritik seviyeyi aştı.</p>
                        
                        <div class="stats">
                            <p><strong>Ders:</strong> %s - %s</p>
                            <p><strong>Devamsız Olduğunuz Ders:</strong> %d / %d</p>
                            <p><strong>Devamsızlık Oranı:</strong> %%.1f%%</p>
                        </div>
                        
                        <p>Devamsızlık limitini aştınız. Lütfen acilen dersin öğretim üyesiyle görüşünüz.</p>
                        <p>Mazeretiniz varsa, belgenizi öğrenci işlerine sunmanız gerekmektedir.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Smart Campus. Tüm hakları saklıdır.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, courseCode, courseName, courseCode, courseName, absentCount, totalSessions, absencePercent);
    }
}
