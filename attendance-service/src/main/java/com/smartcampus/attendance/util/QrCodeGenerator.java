package com.smartcampus.attendance.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
public class QrCodeGenerator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    /**
     * QR kod için Base64 token oluşturur
     */
    public String generateQrToken(Long sessionId) {
        try {
            Map<String, Object> payload = Map.of(
                    "sessionId", sessionId,
                    "token", UUID.randomUUID().toString(),
                    "timestamp", LocalDateTime.now().toString());
            String json = objectMapper.writeValueAsString(payload);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("QR kod oluşturulamadı", e);
        }
    }

    /**
     * QR kod içinde gösterilecek URL'i oluşturur
     * Telefon kamerasıyla okutunca bu URL açılır
     */
    public String generateQrCode(Long sessionId) {
        String token = generateQrToken(sessionId);
        // frontendUrl null olursa varsayılan kullan
        String baseUrl = (frontendUrl != null && !frontendUrl.isBlank())
                ? frontendUrl
                : "http://localhost:3000";
        // URL formatı: http://IP:3000/attendance/give/18?qr=BASE64_TOKEN
        return baseUrl + "/attendance/give/" + sessionId + "?qr="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    /**
     * QR kod URL'inden görsel QR resmi oluşturur
     */
    public String getQrCodeUrl(String qrCodeContent) {
        String encoded = URLEncoder.encode(qrCodeContent, StandardCharsets.UTF_8);
        return "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + encoded;
    }

    /**
     * QR URL'den token'ı parse eder
     */
    public QrCodeData parseQrCode(String qrTokenOrUrl) {
        try {
            String token = qrTokenOrUrl;

            // Eğer URL gelirse, token kısmını çıkar
            if (qrTokenOrUrl.contains("?qr=")) {
                String[] parts = qrTokenOrUrl.split("\\?qr=");
                if (parts.length > 1) {
                    token = java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                }
            }

            String json = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(json, Map.class);

            Long sessionId = ((Number) payload.get("sessionId")).longValue();
            String tokenValue = (String) payload.get("token");
            String timestamp = (String) payload.get("timestamp");

            return new QrCodeData(sessionId, tokenValue, LocalDateTime.parse(timestamp));
        } catch (Exception e) {
            throw new IllegalArgumentException("Geçersiz QR kod formatı");
        }
    }

    public boolean isQrCodeExpired(String qrCode, int validitySeconds) {
        try {
            QrCodeData data = parseQrCode(qrCode);
            return data.timestamp().plusSeconds(validitySeconds).isBefore(LocalDateTime.now());
        } catch (Exception e) {
            return true;
        }
    }

    public record QrCodeData(Long sessionId, String token, LocalDateTime timestamp) {
    }
}
