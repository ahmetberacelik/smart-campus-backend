package com.smartcampus.academic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptResponse {

    private Long studentId;
    private String studentNumber;
    private String studentName;
    private String departmentName;
    private BigDecimal cgpa;
    private Integer totalCredits;
    private Integer completedCredits;
    private List<SemesterRecord> semesters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemesterRecord {
        private String semester;
        private Integer year;
        private BigDecimal gpa;
        private Integer credits;
        private List<CourseRecord> courses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseRecord {
        private String courseCode;
        private String courseName;
        private Integer credits;
        private String letterGrade;
        private BigDecimal gradePoint;
    }
}
