package com.smartcampus.academic.dto.request;

import com.smartcampus.academic.entity.Schedule;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequest {

    @NotNull(message = "Bölüm ID zorunludur")
    private Long sectionId;

    @NotNull(message = "Gün zorunludur")
    private Schedule.DayOfWeek dayOfWeek;

    @NotNull(message = "Başlangıç saati zorunludur")
    private LocalTime startTime;

    @NotNull(message = "Bitiş saati zorunludur")
    private LocalTime endTime;

    @NotNull(message = "Derslik ID zorunludur")
    private Long classroomId;
}
