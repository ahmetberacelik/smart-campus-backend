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
public class UpdateCourseRequest {

    @Size(max = 150, message = "Ders adı en fazla 150 karakter olabilir")
    private String name;

    private String description;

    @Min(value = 1, message = "Kredi en az 1 olmalıdır")
    @Max(value = 10, message = "Kredi en fazla 10 olabilir")
    private Integer credits;

    @Min(value = 1, message = "ECTS en az 1 olmalıdır")
    @Max(value = 30, message = "ECTS en fazla 30 olabilir")
    private Integer ects;

    private String syllabusUrl;
}
