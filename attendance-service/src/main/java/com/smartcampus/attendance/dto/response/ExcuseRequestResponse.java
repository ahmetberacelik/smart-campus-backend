package com.smartcampus.attendance.dto.response;

import com.smartcampus.attendance.entity.ExcuseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcuseRequestResponse {
    private Long id;
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private Long sessionId;
    private String courseCode;
    private LocalDate date;
    private String reason;
    private String documentUrl;
    private ExcuseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewerNotes;
}
