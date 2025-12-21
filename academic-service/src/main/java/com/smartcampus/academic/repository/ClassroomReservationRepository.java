package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.ClassroomReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ClassroomReservationRepository extends JpaRepository<ClassroomReservation, Long> {

    // Kullanıcının rezervasyonları
    List<ClassroomReservation> findByUserIdOrderByReservationDateDesc(Long userId);

    Page<ClassroomReservation> findByUserIdOrderByReservationDateDesc(Long userId, Pageable pageable);

    // Dersliğin rezervasyonları
    List<ClassroomReservation> findByClassroomIdAndReservationDateOrderByStartTimeAsc(Long classroomId, LocalDate date);

    // Onay bekleyenler
    List<ClassroomReservation> findByStatusOrderByCreatedAtAsc(ClassroomReservation.ReservationStatus status);

    Page<ClassroomReservation> findByStatusOrderByCreatedAtAsc(ClassroomReservation.ReservationStatus status,
            Pageable pageable);

    // Çakışma kontrolü - Onaylı veya bekleyen rezervasyonlar
    @Query("SELECT r FROM ClassroomReservation r WHERE r.classroom.id = :classroomId " +
            "AND r.reservationDate = :date AND r.status IN ('PENDING', 'APPROVED') " +
            "AND NOT (r.endTime <= :startTime OR r.startTime >= :endTime)")
    List<ClassroomReservation> findConflictingReservations(
            @Param("classroomId") Long classroomId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    // Belirli tarih aralığındaki rezervasyonlar
    List<ClassroomReservation> findByClassroomIdAndReservationDateBetweenAndStatusIn(
            Long classroomId, LocalDate startDate, LocalDate endDate,
            List<ClassroomReservation.ReservationStatus> statuses);

    // Belirli tarihteki onaylı rezervasyonlar
    @Query("SELECT r FROM ClassroomReservation r WHERE r.reservationDate = :date AND r.status = 'APPROVED'")
    List<ClassroomReservation> findApprovedReservationsForDate(@Param("date") LocalDate date);
}
