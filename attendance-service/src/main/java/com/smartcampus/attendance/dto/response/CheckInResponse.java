package com.smartcampus.attendance.dto.response;

import com.smartcampus.attendance.entity.AttendanceStatus;
import com.smartcampus.attendance.entity.CheckInMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private Long sessionId;
    private LocalDateTime checkInTime;
    private Double distance;
    private CheckInMethod method;
    private AttendanceStatus status;
}
