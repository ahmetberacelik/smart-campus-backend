package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.request.CreateReservationRequest;
import com.smartcampus.academic.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ClassroomReservationService {

    ReservationResponse createReservation(Long userId, CreateReservationRequest request);

    ReservationResponse getReservationById(Long id);

    List<ReservationResponse> getMyReservations(Long userId);

    Page<ReservationResponse> getMyReservationsPaged(Long userId, Pageable pageable);

    List<ReservationResponse> getReservationsByClassroom(Long classroomId, LocalDate date);

    Page<ReservationResponse> getPendingReservations(Pageable pageable);

    ReservationResponse approveReservation(Long reservationId, Long adminId);

    ReservationResponse rejectReservation(Long reservationId, Long adminId, String reason);

    void cancelReservation(Long reservationId, Long userId);

    List<ReservationResponse> getAvailableSlots(Long classroomId, LocalDate date);
}
