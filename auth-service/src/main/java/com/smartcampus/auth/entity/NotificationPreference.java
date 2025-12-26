package com.smartcampus.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Email Preferences
    @Column(name = "email_academic")
    @Builder.Default
    private Boolean emailAcademic = true;

    @Column(name = "email_attendance")
    @Builder.Default
    private Boolean emailAttendance = true;

    @Column(name = "email_meal")
    @Builder.Default
    private Boolean emailMeal = false;

    @Column(name = "email_event")
    @Builder.Default
    private Boolean emailEvent = true;

    @Column(name = "email_payment")
    @Builder.Default
    private Boolean emailPayment = true;

    @Column(name = "email_system")
    @Builder.Default
    private Boolean emailSystem = true;

    // Push Preferences
    @Column(name = "push_academic")
    @Builder.Default
    private Boolean pushAcademic = true;

    @Column(name = "push_attendance")
    @Builder.Default
    private Boolean pushAttendance = true;

    @Column(name = "push_meal")
    @Builder.Default
    private Boolean pushMeal = true;

    @Column(name = "push_event")
    @Builder.Default
    private Boolean pushEvent = true;

    @Column(name = "push_payment")
    @Builder.Default
    private Boolean pushPayment = true;

    @Column(name = "push_system")
    @Builder.Default
    private Boolean pushSystem = false;

    // SMS Preferences
    @Column(name = "sms_attendance")
    @Builder.Default
    private Boolean smsAttendance = true;

    @Column(name = "sms_payment")
    @Builder.Default
    private Boolean smsPayment = false;

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
}
