package com.smartcampus.attendance.dto.response;

import com.smartcampus.attendance.entity.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    private Long sectionId;
    private String courseCode;
    private String courseName;
    private String sectionNumber;
    private String instructorName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double latitude;
    private Double longitude;
    private Integer geofenceRadius;
    private String qrCode;
    private String qrCodeUrl;
    private SessionStatus status;
    private Integer enrolledCount;
    private Integer presentCount;
    private Integer absentCount;
    private Double attendanceRate;
    private String classroomName;
}
