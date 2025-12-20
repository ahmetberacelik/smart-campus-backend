package com.smartcampus.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false)
    private Integer capacity = 100;

    @Column(name = "registered_count", nullable = false)
    private Integer registeredCount = 0;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;

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
    public boolean hasAvailableCapacity() {
        return registeredCount < capacity;
    }

    public int getAvailableSpots() {
        return Math.max(0, capacity - registeredCount);
    }

    public boolean isRegistrationOpen() {
        if (status != EventStatus.PUBLISHED) return false;
        if (registrationDeadline != null && LocalDateTime.now().isAfter(registrationDeadline)) return false;
        return true;
    }

    public void incrementRegisteredCount() {
        this.registeredCount++;
    }

    public void decrementRegisteredCount() {
        if (this.registeredCount > 0) {
            this.registeredCount--;
        }
    }

    public enum EventCategory {
        CONFERENCE, WORKSHOP, SEMINAR, SOCIAL, SPORTS, CULTURAL, CAREER
    }

    public enum EventStatus {
        DRAFT, PUBLISHED, CANCELLED, COMPLETED
    }
}
