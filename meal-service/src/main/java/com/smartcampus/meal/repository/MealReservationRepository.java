package com.smartcampus.meal.repository;

import com.smartcampus.meal.entity.MealMenu;
import com.smartcampus.meal.entity.MealReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealReservationRepository extends JpaRepository<MealReservation, Long> {
    
    Optional<MealReservation> findByQrCode(String qrCode);
    
    List<MealReservation> findByUserIdAndStatusOrderByReservationDateDesc(
            Long userId, MealReservation.ReservationStatus status);
    
    Page<MealReservation> findByUserIdOrderByReservationDateDesc(Long userId, Pageable pageable);
    
    @Query("SELECT r FROM MealReservation r WHERE r.userId = :userId " +
           "AND r.reservationDate >= :today ORDER BY r.reservationDate")
    List<MealReservation> findUpcomingReservations(
            @Param("userId") Long userId, @Param("today") LocalDate today);
    
    // Aynı gün, aynı öğün için tekrar rezervasyon kontrolü
    boolean existsByUserIdAndReservationDateAndMealTypeAndStatusNot(
            Long userId, LocalDate date, MealMenu.MealType mealType, 
            MealReservation.ReservationStatus excludeStatus);
    
    // Günlük burs kullanım sayısı
    @Query("SELECT COUNT(r) FROM MealReservation r WHERE r.userId = :userId " +
           "AND r.reservationDate = :date AND r.isScholarshipUsed = true " +
           "AND r.status != 'CANCELLED'")
    int countScholarshipUsedToday(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // Belirli tarihteki tüm rezervasyonlar (kafeterya personeli için)
    List<MealReservation> findByCafeteriaIdAndReservationDateAndMealType(
            Long cafeteriaId, LocalDate date, MealMenu.MealType mealType);
}
