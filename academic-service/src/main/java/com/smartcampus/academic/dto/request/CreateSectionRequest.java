package com.smartcampus.academic.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSectionRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Section number is required")
    @Size(max = 10, message = "Section number must be at most 10 characters")
    private String sectionNumber;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be at least 2020")
    private Integer year;

    @NotNull(message = "Instructor ID is required")
    private Long instructorId;

    private Long classroomId;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity = 40;

    private String scheduleJson;
}