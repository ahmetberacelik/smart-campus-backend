package com.smartcampus.auth.controller;

import com.smartcampus.auth.dto.NotificationPreferenceRequest;
import com.smartcampus.auth.dto.NotificationPreferenceResponse;
import com.smartcampus.auth.dto.NotificationResponse;
import com.smartcampus.auth.dto.response.ApiResponse;
import com.smartcampus.auth.security.CurrentUser;
import com.smartcampus.auth.security.CustomUserDetails;
import com.smartcampus.auth.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Bildirim yönetimi işlemleri")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Bildirim listesi", description = "Kullanıcının bildirimlerini getirir")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<NotificationResponse> notifications = notificationService.getNotifications(
                currentUser.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Okunmamış bildirim sayısı", description = "Okunmamış bildirim sayısını döndürür")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @CurrentUser CustomUserDetails currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getId());
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Bildirimi okundu işaretle", description = "Belirtilen bildirimi okundu olarak işaretler")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {
        NotificationResponse notification = notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Bildirim okundu olarak işaretlendi", notification));
    }

    @PutMapping("/mark-all-read")
    @Operation(summary = "Tüm bildirimleri okundu işaretle", description = "Kullanıcının tüm bildirimlerini okundu işaretler")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
            @CurrentUser CustomUserDetails currentUser) {
        int count = notificationService.markAllAsRead(currentUser.getId());
        Map<String, Integer> response = new HashMap<>();
        response.put("markedCount", count);
        return ResponseEntity.ok(ApiResponse.success("Tüm bildirimler okundu olarak işaretlendi", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Bildirimi sil", description = "Belirtilen bildirimi siler")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {
        notificationService.deleteNotification(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Bildirim silindi"));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Bildirim tercihlerini getir", description = "Kullanıcının bildirim tercihlerini getirir")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @CurrentUser CustomUserDetails currentUser) {
        NotificationPreferenceResponse preferences = notificationService.getPreferences(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(preferences));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Bildirim tercihlerini güncelle", description = "Kullanıcının bildirim tercihlerini günceller")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @CurrentUser CustomUserDetails currentUser,
            @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse preferences = notificationService.updatePreferences(
                currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Bildirim tercihleri güncellendi", preferences));
    }
}
