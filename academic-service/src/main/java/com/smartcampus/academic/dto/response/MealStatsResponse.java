package com.smartcampus.academic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Yemek kullanım analitik verileri
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealStatsResponse {

    // Genel istatistikler
    private Long totalReservationsToday;
    private Long totalReservationsThisWeek;
    private Long totalReservationsThisMonth;
    private Long usedReservationsToday;
    private Long cancelledReservationsToday;

    // Burslu vs ücretli dağılımı
    private Long scholarshipMeals;
    private Long paidMeals;

    // Yemekhane bazlı kullanım
    private List<CafeteriaStats> cafeteriaStats;

    // Öğün bazlı dağılım
    private Map<String, Long> mealTypeDistribution; // LUNCH, DINNER

    // Haftalık trend
    private List<DailyMealStats> weeklyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CafeteriaStats {
        private Long cafeteriaId;
        private String cafeteriaName;
        private Long todayReservations;
        private Long todayUsed;
        private Integer capacity;
        private Double utilizationRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyMealStats {
        private String date;
        private Long reservations;
        private Long used;
        private Long cancelled;
    }
}
