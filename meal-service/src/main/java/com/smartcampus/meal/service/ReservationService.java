package com.smartcampus.meal.service;

import com.smartcampus.meal.dto.request.ReservationRequest;
import com.smartcampus.meal.dto.response.ReservationResponse;
import com.smartcampus.meal.entity.MealMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    
    ReservationResponse createReservation(Long userId, ReservationRequest request);
    
    ReservationResponse getReservationById(Long reservationId);
    
    ReservationResponse getReservationByQrCode(String qrCode);
    
    List<ReservationResponse> getUpcomingReservations(Long userId);
    
    Page<ReservationResponse> getUserReservations(Long userId, Pageable pageable);
    
    void cancelReservation(Long userId, Long reservationId);
    
    // Cafeteria staff - QR ile yemek kullanımı
    ReservationResponse useReservation(String qrCode);
    
    // Cafeteria staff - günlük rezervasyon listesi
    List<ReservationResponse> getDailyReservations(Long cafeteriaId, LocalDate date, MealMenu.MealType mealType);
    
    // Günlük istatistikler
    long countTodayReservations(Long cafeteriaId);
    
    long countUsedReservationsToday(Long cafeteriaId);
}
