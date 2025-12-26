package com.smartcampus.academic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin Dashboard ana istatistikleri
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // Kullanıcı İstatistikleri
    private Long totalUsers;
    private Long totalStudents;
    private Long totalFaculty;
    private Long totalAdmins;

    // Akademik İstatistikler
    private Long totalDepartments;
    private Long totalCourses;
    private Long totalSections;
    private Long totalEnrollments;

    // Yoklama İstatistikleri
    private Long totalAttendanceSessions;
    private Double averageAttendanceRate;

    // Yemek İstatistikleri
    private Long totalMealReservationsToday;
    private Long totalMealReservationsThisMonth;

    // Etkinlik İstatistikleri
    private Long totalEvents;
    private Long upcomingEvents;
    private Long totalEventRegistrations;

    // Sistem Durumu
    private String systemHealth;
    private String lastUpdated;
}
