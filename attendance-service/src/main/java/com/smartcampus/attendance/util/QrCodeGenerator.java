package com.smartcampus.attendance.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public String generateQrCode(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID null olamaz");
        }
        try {
            Map<String, Object> payload = Map.of(
                    "sessionId", sessionId,
                    "token", UUID.randomUUID().toString(),
                    "timestamp", LocalDateTime.now().toString()
            );
            String json = objectMapper.writeValueAsString(payload);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("QR kod oluşturulamadı", e);
        }
    }

    public String getQrCodeUrl(String qrCode) {
        String encoded = URLEncoder.encode(qrCode, StandardCharsets.UTF_8);
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encoded;
    }

    public QrCodeData parseQrCode(String qrCode) {
        try {
            String json = new String(Base64.getDecoder().decode(qrCode), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(json, Map.class);

            Long sessionId = ((Number) payload.get("sessionId")).longValue();
            String token = (String) payload.get("token");
            String timestamp = (String) payload.get("timestamp");

            return new QrCodeData(sessionId, token, LocalDateTime.parse(timestamp));
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

    public record QrCodeData(Long sessionId, String token, LocalDateTime timestamp) {}
}
