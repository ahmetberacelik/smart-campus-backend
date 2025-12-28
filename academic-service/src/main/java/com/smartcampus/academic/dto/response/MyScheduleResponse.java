package com.smartcampus.academic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Öğrencinin/öğretim üyesinin ders programı response'u.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyScheduleResponse {
    private String semester;
    private Integer year;
    private List<ScheduleEntryResponse> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleEntryResponse {
        private String id;
        private String sectionId;
        private String courseCode;
        private String courseName;
        private String sectionNumber;
        private String instructorName;
        private Integer dayOfWeek; // 1=Monday, 7=Sunday
        private String startTime; // HH:mm
        private String endTime; // HH:mm
        private String room;
        private String building;
        private String semester;
        private Integer year;
    }
}
