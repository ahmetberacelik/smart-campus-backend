package com.smartcampus.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "qr_code", nullable = false, unique = true, length = 100)
    private String qrCode;

    @Column(name = "checked_in")
    private Boolean checkedIn = false;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    @Column(name = "custom_fields_json", columnDefinition = "JSON")
    private String customFieldsJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        registrationDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isActive() {
        return status == RegistrationStatus.REGISTERED || status == RegistrationStatus.WAITLIST;
    }

    public boolean canCheckIn() {
        return status == RegistrationStatus.REGISTERED && !Boolean.TRUE.equals(checkedIn);
    }

    public void checkIn() {
        this.checkedIn = true;
        this.checkedInAt = LocalDateTime.now();
        this.status = RegistrationStatus.ATTENDED;
    }

    public boolean isCancellable() {
        return status == RegistrationStatus.REGISTERED || status == RegistrationStatus.WAITLIST;
    }

    public enum RegistrationStatus {
        REGISTERED, WAITLIST, CANCELLED, ATTENDED
    }
}
