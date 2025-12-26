package com.smartcampus.academic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Yoklama analitik verileri
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatsResponse {

    // Genel yoklama istatistikleri
    private Double overallAttendanceRate;
    private Long totalSessions;
    private Long totalRecords;

    // Devamsızlık uyarıları
    private Long studentsWithWarning; // %20-30 devamsızlık
    private Long studentsWithCritical; // %30+ devamsızlık

    // Ders bazlı yoklama oranları
    private List<CourseAttendanceStats> courseStats;

    // Son 7 gün trend
    private List<DailyAttendanceStats> weeklyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseAttendanceStats {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Double attendanceRate;
        private Long sessionCount;
        private Long enrolledStudents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAttendanceStats {
        private String date;
        private Long sessionCount;
        private Double attendanceRate;
    }
}
