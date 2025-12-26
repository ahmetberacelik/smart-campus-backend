package com.smartcampus.academic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Akademik performans analitik verileri
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicStatsResponse {

    // Genel GPA İstatistikleri
    private Double averageGpa;
    private Double averageCgpa;
    private Double highestGpa;
    private Double lowestGpa;

    // Bölüm bazlı GPA ortalamaları
    private List<DepartmentGpaStats> departmentStats;

    // Not dağılımı (A, B, C, D, F yüzdeleri)
    private Map<String, Double> gradeDistribution;

    // Geçme/Kalma oranları
    private Double passRate;
    private Double failRate;

    // En yüksek ve düşük performanslı öğrenciler
    private Long studentsAbove3; // GPA > 3.0
    private Long studentsBetween2And3; // 2.0 <= GPA < 3.0
    private Long studentsBelow2; // GPA < 2.0 (riskli)

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentGpaStats {
        private Long departmentId;
        private String departmentName;
        private String departmentCode;
        private Double averageGpa;
        private Long studentCount;
    }
}
