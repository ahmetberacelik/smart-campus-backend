package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.CreateReservationRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.ReservationResponse;
import com.smartcampus.academic.security.CustomUserDetails;
import com.smartcampus.academic.service.ClassroomReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/classroom-reservations")
@RequiredArgsConstructor
public class ClassroomReservationController {

    private final ClassroomReservationService reservationService;

    /**
     * Rezervasyon oluştur
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse reservation = reservationService.createReservation(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rezervasyon oluşturuldu, onay bekleniyor", reservation));
    }

    /**
     * Rezervasyon detayı
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    /**
     * Benim rezervasyonlarım
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReservationResponse> reservations = reservationService.getMyReservations(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    /**
     * Derslik rezervasyonları (belirli tarih)
     */
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsByClassroom(
            @PathVariable Long classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ReservationResponse> reservations = reservationService.getReservationsByClassroom(classroomId, date);
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    /**
     * Müsaitlik kontrolü (dolu slotları döner)
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAvailableSlots(
            @RequestParam Long classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ReservationResponse> occupiedSlots = reservationService.getAvailableSlots(classroomId, date);
        return ResponseEntity.ok(ApiResponse.success(occupiedSlots));
    }

    /**
     * Onay bekleyen rezervasyonlar (Admin)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getPendingReservations(Pageable pageable) {
        Page<ReservationResponse> reservations = reservationService.getPendingReservations(pageable);
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    /**
     * Rezervasyon onayla (Admin)
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationResponse>> approveReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReservationResponse reservation = reservationService.approveReservation(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Rezervasyon onaylandı", reservation));
    }

    /**
     * Rezervasyon reddet (Admin)
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationResponse>> rejectReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String reason) {
        ReservationResponse reservation = reservationService.rejectReservation(id, userDetails.getId(), reason);
        return ResponseEntity.ok(ApiResponse.success("Rezervasyon reddedildi", reservation));
    }

    /**
     * Rezervasyon iptal et
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reservationService.cancelReservation(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Rezervasyon iptal edildi", null));
    }
}
