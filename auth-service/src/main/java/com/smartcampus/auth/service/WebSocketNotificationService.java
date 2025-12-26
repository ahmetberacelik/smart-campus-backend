package com.smartcampus.auth.service;

import com.smartcampus.auth.dto.NotificationResponse;
import com.smartcampus.auth.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket ile real-time bildirim gönderme servisi.
 * 
 * Kullanım:
 * webSocketNotificationService.sendToUser(userId, notification);
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Belirli bir kullanıcıya real-time bildirim gönder.
     * Kullanıcı /user/queue/notifications'a subscribe olmalı.
     */
    public void sendToUser(Long userId, NotificationResponse notification) {
        try {
            String destination = "/queue/notifications";
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    destination,
                    notification);
            log.info("WebSocket notification sent: userId={}, title={}", userId, notification.getTitle());
        } catch (Exception e) {
            log.error("WebSocket notification failed: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * Belirli bir kullanıcıya Notification entity'den bildirim gönder.
     */
    public void sendToUser(Long userId, Notification notification) {
        NotificationResponse response = NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .category(notification.getCategory())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .dataJson(notification.getDataJson())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();

        sendToUser(userId, response);
    }

    /**
     * Tüm kullanıcılara broadcast bildirim gönder (örn: sistem duyurusu).
     */
    public void broadcast(NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.info("WebSocket broadcast sent: title={}", notification.getTitle());
        } catch (Exception e) {
            log.error("WebSocket broadcast failed: error={}", e.getMessage());
        }
    }

    /**
     * Okunmamış bildirim sayısı güncellemesi gönder.
     */
    public void sendUnreadCount(Long userId, long count) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/unread-count",
                    count);
            log.debug("WebSocket unread count sent: userId={}, count={}", userId, count);
        } catch (Exception e) {
            log.error("WebSocket unread count failed: userId={}, error={}", userId, e.getMessage());
        }
    }
}
