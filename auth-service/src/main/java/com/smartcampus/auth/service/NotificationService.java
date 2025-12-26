package com.smartcampus.auth.service;

import com.smartcampus.auth.dto.NotificationPreferenceRequest;
import com.smartcampus.auth.dto.NotificationPreferenceResponse;
import com.smartcampus.auth.dto.NotificationResponse;
import com.smartcampus.auth.entity.*;
import com.smartcampus.auth.repository.NotificationPreferenceRepository;
import com.smartcampus.auth.repository.NotificationRepository;
import com.smartcampus.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final EmailService emailService;

    /**
     * Kullanıcının bildirimlerini getir (sayfalı)
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Kullanıcının okunmamış bildirim sayısını getir
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Bildirimi okundu olarak işaretle
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bu bildirime erişim yetkiniz yok");
        }

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        log.info("Bildirim okundu: id={}, userId={}", notificationId, userId);
        return mapToResponse(saved);
    }

    /**
     * Tüm bildirimleri okundu olarak işaretle
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsReadByUserId(userId);
        log.info("Tüm bildirimler okundu: userId={}, count={}", userId, count);
        return count;
    }

    /**
     * Bildirimi sil
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bu bildirimi silme yetkiniz yok");
        }

        notificationRepository.delete(notification);
        log.info("Bildirim silindi: id={}, userId={}", notificationId, userId);
    }

    /**
     * Kullanıcının bildirim tercihlerini getir
     */
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(Long userId) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        return mapToPreferenceResponse(pref);
    }

    /**
     * Kullanıcının bildirim tercihlerini güncelle
     */
    @Transactional
    public NotificationPreferenceResponse updatePreferences(Long userId, NotificationPreferenceRequest request) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        // Email Preferences
        if (request.getEmailAcademic() != null)
            pref.setEmailAcademic(request.getEmailAcademic());
        if (request.getEmailAttendance() != null)
            pref.setEmailAttendance(request.getEmailAttendance());
        if (request.getEmailMeal() != null)
            pref.setEmailMeal(request.getEmailMeal());
        if (request.getEmailEvent() != null)
            pref.setEmailEvent(request.getEmailEvent());
        if (request.getEmailPayment() != null)
            pref.setEmailPayment(request.getEmailPayment());
        if (request.getEmailSystem() != null)
            pref.setEmailSystem(request.getEmailSystem());

        // Push Preferences
        if (request.getPushAcademic() != null)
            pref.setPushAcademic(request.getPushAcademic());
        if (request.getPushAttendance() != null)
            pref.setPushAttendance(request.getPushAttendance());
        if (request.getPushMeal() != null)
            pref.setPushMeal(request.getPushMeal());
        if (request.getPushEvent() != null)
            pref.setPushEvent(request.getPushEvent());
        if (request.getPushPayment() != null)
            pref.setPushPayment(request.getPushPayment());
        if (request.getPushSystem() != null)
            pref.setPushSystem(request.getPushSystem());

        // SMS Preferences
        if (request.getSmsAttendance() != null)
            pref.setSmsAttendance(request.getSmsAttendance());
        if (request.getSmsPayment() != null)
            pref.setSmsPayment(request.getSmsPayment());

        NotificationPreference saved = preferenceRepository.save(pref);
        log.info("Bildirim tercihleri güncellendi: userId={}", userId);
        return mapToPreferenceResponse(saved);
    }

    /**
     * Yeni bildirim oluştur (diğer servisler tarafından çağrılır)
     */
    @Transactional
    public Notification createNotification(Long userId, NotificationType type, NotificationCategory category,
            String title, String message, String dataJson) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .category(category)
                .title(title)
                .message(message)
                .dataJson(dataJson)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Bildirim oluşturuldu: userId={}, category={}, title={}", userId, category, title);

        // Real-time WebSocket bildirim gönder
        webSocketNotificationService.sendToUser(userId, saved);

        // Email bildirim gönder (kullanıcı tercihine göre)
        sendEmailIfEnabled(user, category, title, message);

        return saved;
    }

    /**
     * Kullanıcı tercihine göre email gönder
     */
    private void sendEmailIfEnabled(User user, NotificationCategory category, String title, String message) {
        try {
            NotificationPreference pref = preferenceRepository.findByUserId(user.getId())
                    .orElse(null);

            if (pref == null) {
                return; // Tercih yoksa email gönderme
            }

            boolean shouldSendEmail = switch (category) {
                case ACADEMIC -> Boolean.TRUE.equals(pref.getEmailAcademic());
                case ATTENDANCE -> Boolean.TRUE.equals(pref.getEmailAttendance());
                case MEAL -> Boolean.TRUE.equals(pref.getEmailMeal());
                case EVENT -> Boolean.TRUE.equals(pref.getEmailEvent());
                case PAYMENT -> Boolean.TRUE.equals(pref.getEmailPayment());
                case SYSTEM -> Boolean.TRUE.equals(pref.getEmailSystem());
            };

            if (shouldSendEmail) {
                String subject = "Smart Campus - " + getCategoryDisplayName(category);
                String userName = user.getFirstName() + " " + user.getLastName();
                emailService.sendNotificationEmail(
                        user.getEmail(),
                        userName,
                        subject,
                        title,
                        message,
                        category.name());
                log.info("Email bildirim gönderildi: userId={}, category={}", user.getId(), category);
            }
        } catch (Exception e) {
            log.warn("Email bildirim gönderilemedi: userId={}, error={}", user.getId(), e.getMessage());
            // Email hatası bildirim oluşturmayı engellemez
        }
    }

    private String getCategoryDisplayName(NotificationCategory category) {
        return switch (category) {
            case ACADEMIC -> "Akademik Bildirim";
            case ATTENDANCE -> "Yoklama Bildirimi";
            case MEAL -> "Yemek Bildirimi";
            case EVENT -> "Etkinlik Bildirimi";
            case PAYMENT -> "Ödeme Bildirimi";
            case SYSTEM -> "Sistem Bildirimi";
        };
    }

    /**
     * Varsayılan bildirim tercihleri oluştur
     */
    private NotificationPreference createDefaultPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        NotificationPreference pref = NotificationPreference.builder()
                .user(user)
                .build();

        return preferenceRepository.save(pref);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .category(notification.getCategory())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .dataJson(notification.getDataJson())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private NotificationPreferenceResponse mapToPreferenceResponse(NotificationPreference pref) {
        return NotificationPreferenceResponse.builder()
                .emailAcademic(pref.getEmailAcademic())
                .emailAttendance(pref.getEmailAttendance())
                .emailMeal(pref.getEmailMeal())
                .emailEvent(pref.getEmailEvent())
                .emailPayment(pref.getEmailPayment())
                .emailSystem(pref.getEmailSystem())
                .pushAcademic(pref.getPushAcademic())
                .pushAttendance(pref.getPushAttendance())
                .pushMeal(pref.getPushMeal())
                .pushEvent(pref.getPushEvent())
                .pushPayment(pref.getPushPayment())
                .pushSystem(pref.getPushSystem())
                .smsAttendance(pref.getSmsAttendance())
                .smsPayment(pref.getSmsPayment())
                .build();
    }
}
