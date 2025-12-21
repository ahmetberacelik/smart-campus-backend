package com.smartcampus.academic.dto.response;

import com.smartcampus.academic.entity.ClassroomReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long classroomId;
    private String classroomName;
    private Long userId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private ClassroomReservation.ReservationStatus status;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private String notes;
    private Boolean isCancellable;
    private LocalDateTime createdAt;
}
