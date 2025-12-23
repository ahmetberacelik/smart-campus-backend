package com.smartcampus.academic.dto.response;

import com.smartcampus.academic.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * Program oluşturma (generate schedule) cevabı.
 * Frontend tarafındaki GeneratedSchedule tipine uyumlu olacak şekilde basit tutulmuştur.
 * Şu aşamada sadece görsel olarak alternatifleri göstermek için kullanılıyor;
 * gerçek CSP algoritması ileride eklenecek.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedScheduleResponse {

    private Long id;
    private String semester;
    private Integer year;
    private List<GeneratedScheduleEntry> entries;
    private int conflicts;
    private double score;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedScheduleEntry {
        private Long sectionId;
        private String courseCode;
        private String courseName;
        private Schedule.DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private String classroomName;
    }
}


