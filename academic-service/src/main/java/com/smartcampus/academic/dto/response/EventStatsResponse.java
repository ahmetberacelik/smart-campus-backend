package com.smartcampus.academic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Etkinlik analitik verileri
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatsResponse {

    // Genel istatistikler
    private Long totalEvents;
    private Long upcomingEvents;
    private Long pastEvents;
    private Long cancelledEvents;

    // Kayıt istatistikleri
    private Long totalRegistrations;
    private Long checkedInCount;
    private Double averageCheckInRate;

    // Kategori bazlı dağılım
    private Map<String, Long> categoryDistribution; // CONFERENCE, WORKSHOP, SOCIAL, SPORTS

    // En popüler etkinlikler
    private List<PopularEventStats> popularEvents;

    // Aylık trend
    private List<MonthlyEventStats> monthlyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularEventStats {
        private Long eventId;
        private String title;
        private String category;
        private Long registrations;
        private Long checkedIn;
        private Integer capacity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyEventStats {
        private String month;
        private Long eventCount;
        private Long registrationCount;
    }
}
