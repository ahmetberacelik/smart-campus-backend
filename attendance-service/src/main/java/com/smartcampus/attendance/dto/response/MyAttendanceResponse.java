package com.smartcampus.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyAttendanceResponse {
    private String courseCode;
    private String courseName;
    private String sectionNumber;
    private Integer totalSessions;
    private Integer presentCount;
    private Integer absentCount;
    private Integer excusedCount;
    private Double attendancePercentage;
    private String status;
    private List<SessionAttendance> sessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionAttendance {
        private Long sessionId;
        private LocalDate date;
        private LocalTime startTime;
        private String status;
        private LocalTime checkInTime;
        private String excuseStatus;
    }
}
