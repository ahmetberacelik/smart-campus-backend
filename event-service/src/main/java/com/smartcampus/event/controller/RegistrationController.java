package com.smartcampus.event.controller;

import com.smartcampus.event.dto.request.RegistrationRequest;
import com.smartcampus.event.dto.response.ApiResponse;
import com.smartcampus.event.dto.response.RegistrationResponse;
import com.smartcampus.event.security.CustomUserDetails;
import com.smartcampus.event.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * Etkinliğe kayıt ol
     */
    @PostMapping("/{eventId}/register")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerForEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) RegistrationRequest request) {
        String customFields = request != null ? request.getCustomFieldsJson() : null;
        RegistrationResponse registration = registrationService.registerForEvent(eventId, userDetails.getId(), customFields);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kayıt başarılı", registration));
    }

    /**
     * Kayıt iptal
     */
    @DeleteMapping("/{eventId}/register")
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        registrationService.cancelRegistration(eventId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Kayıt iptal edildi", null));
    }

    /**
     * Kayıtlı olduğum etkinlikler
     */
    @GetMapping("/my-registrations")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyRegistrations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<RegistrationResponse> registrations = registrationService.getMyRegistrations(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    /**
     * Etkinliğin katılımcı listesi (Organizatör)
     */
    @GetMapping("/{eventId}/registrations")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<RegistrationResponse>>> getEventRegistrations(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        Page<RegistrationResponse> registrations = registrationService.getEventRegistrationsPaged(
                eventId, userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    /**
     * QR kod ile kayıt sorgula (Staff)
     */
    @GetMapping("/registration/qr/{qrCode}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> getRegistrationByQrCode(
            @PathVariable String qrCode) {
        RegistrationResponse registration = registrationService.getRegistrationByQrCode(qrCode);
        return ResponseEntity.ok(ApiResponse.success(registration));
    }

    /**
     * Check-in yap (Staff)
     */
    @PostMapping("/check-in/{qrCode}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> checkIn(
            @PathVariable String qrCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        RegistrationResponse registration = registrationService.checkIn(qrCode, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Check-in başarılı", registration));
    }

    /**
     * Etkinlik istatistikleri (Organizatör)
     */
    @GetMapping("/{eventId}/stats")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getEventStats(@PathVariable Long eventId) {
        long registered = registrationService.getRegistrationCount(eventId);
        long checkedIn = registrationService.getCheckedInCount(eventId);
        return ResponseEntity.ok(ApiResponse.success(
                java.util.Map.of(
                        "registeredCount", registered,
                        "checkedInCount", checkedIn
                )
        ));
    }
}
