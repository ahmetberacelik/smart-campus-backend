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
public class CreateCourseRequest {

    @NotBlank(message = "Ders kodu zorunludur")
    @Size(max = 20, message = "Ders kodu en fazla 20 karakter olabilir")
    private String code;

    @NotBlank(message = "Ders adı zorunludur")
    @Size(max = 150, message = "Ders adı en fazla 150 karakter olabilir")
    private String name;

    private String description;

    @NotNull(message = "Kredi zorunludur")
    @Min(value = 1, message = "Kredi en az 1 olmalıdır")
    @Max(value = 10, message = "Kredi en fazla 10 olabilir")
    private Integer credits;

    @NotNull(message = "ECTS zorunludur")
    @Min(value = 1, message = "ECTS en az 1 olmalıdır")
    @Max(value = 30, message = "ECTS en fazla 30 olabilir")
    private Integer ects;

    @NotNull(message = "Departman ID zorunludur")
    private Long departmentId;

    private String syllabusUrl;
}
