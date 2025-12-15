package com.smartcampus.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportResponse {
    private Long sectionId;
    private String courseCode;
    private String courseName;
    private Integer totalSessions;
    private List<StudentAttendance> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendance {
        private Long studentId;
        private String studentNumber;
        private String firstName;
        private String lastName;
        private Integer presentCount;
        private Integer absentCount;
        private Integer excusedCount;
        private Double attendancePercentage;
        private String status;
        private Boolean isFlagged;
        private String flagReason;
    }
}
