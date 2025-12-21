package com.smartcampus.academic.dto.response;

import com.smartcampus.academic.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long id;
    private Long sectionId;
    private String sectionCode;
    private String courseName;
    private String courseCode;
    private Schedule.DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long classroomId;
    private String classroomName; // building + room_number
    private Boolean isActive;
    private LocalDateTime createdAt;
}
