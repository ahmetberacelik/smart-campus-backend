package com.smartcampus.academic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "classroom_reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }

    public boolean isCancellable() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.APPROVED;
    }

    public void approve(Long adminId) {
        this.status = ReservationStatus.APPROVED;
        this.approvedBy = adminId;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(Long adminId, String reason) {
        this.status = ReservationStatus.REJECTED;
        this.approvedBy = adminId;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    public boolean conflictsWith(LocalTime otherStart, LocalTime otherEnd) {
        return !(endTime.isBefore(otherStart) || endTime.equals(otherStart) ||
                startTime.isAfter(otherEnd) || startTime.equals(otherEnd));
    }

    public enum ReservationStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
