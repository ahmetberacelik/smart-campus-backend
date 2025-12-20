package com.smartcampus.meal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 3)
    private String currency = "TRY";

    @Column(name = "is_scholarship")
    private Boolean isScholarship = false;

    @Column(name = "daily_scholarship_limit")
    private Integer dailyScholarshipLimit = 2;

    @Column(name = "scholarship_used_today")
    private Integer scholarshipUsedToday = 0;

    @Column(name = "last_scholarship_reset")
    private LocalDate lastScholarshipReset;

    @Column(name = "is_active")
    private Boolean isActive = true;

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

    /**
     * Günlük burs limitini sıfırla (yeni gün başlangıcında)
     */
    public void resetDailyScholarship() {
        LocalDate today = LocalDate.now();
        if (lastScholarshipReset == null || !lastScholarshipReset.equals(today)) {
            scholarshipUsedToday = 0;
            lastScholarshipReset = today;
        }
    }

    /**
     * Burs hakkı kullanılabilir mi?
     */
    public boolean canUseScholarship() {
        resetDailyScholarship();
        return isScholarship && scholarshipUsedToday < dailyScholarshipLimit;
    }

    /**
     * Burs hakkı kullan
     */
    public void useScholarship() {
        if (canUseScholarship()) {
            scholarshipUsedToday++;
        }
    }
}
