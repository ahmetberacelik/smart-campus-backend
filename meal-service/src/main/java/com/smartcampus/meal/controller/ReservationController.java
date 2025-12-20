package com.smartcampus.meal.controller;

import com.smartcampus.meal.dto.request.ReservationRequest;
import com.smartcampus.meal.dto.response.ApiResponse;
import com.smartcampus.meal.dto.response.ReservationResponse;
import com.smartcampus.meal.entity.MealMenu;
import com.smartcampus.meal.security.CustomUserDetails;
import com.smartcampus.meal.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/meals/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Yemek rezervasyonu yap
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse reservation = reservationService.createReservation(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Rezervasyon oluşturuldu", reservation));
    }

    /**
     * Yaklaşan rezervasyonlarım
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getUpcomingReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReservationResponse> reservations = reservationService.getUpcomingReservations(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    /**
     * Tüm rezervasyonlarım (sayfalı)
     */
    @GetMapping("/my-reservations")
    public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        Page<ReservationResponse> reservations = reservationService.getUserReservations(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    /**
     * Rezervasyon detayı
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    /**
     * Rezervasyon iptal et
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        reservationService.cancelReservation(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Rezervasyon iptal edildi"));
    }

    // =========== Kafeterya Personeli Endpoints ===========

    /**
     * QR kod ile yemek kullan
     */
    @PostMapping("/use")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<ReservationResponse>> useReservation(
            @RequestParam String qrCode) {
        ReservationResponse reservation = reservationService.useReservation(qrCode);
        return ResponseEntity.ok(ApiResponse.success("Yemek kullanıldı", reservation));
    }

    /**
     * QR kod ile rezervasyon sorgula
     */
    @GetMapping("/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<ReservationResponse>> scanQRCode(
            @RequestParam String qrCode) {
        ReservationResponse reservation = reservationService.getReservationByQrCode(qrCode);
        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    /**
     * Günlük rezervasyon listesi (personel için)
     */
    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getDailyReservations(
            @RequestParam Long cafeteriaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) MealMenu.MealType mealType) {
        
        if (date == null) date = LocalDate.now();
        if (mealType == null) mealType = MealMenu.MealType.LUNCH;
        
        List<ReservationResponse> reservations = reservationService.getDailyReservations(cafeteriaId, date, mealType);
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }
}
