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
public class UpdateSectionRequest {

    private Long instructorId;

    @Min(value = 1, message = "Kapasite en az 1 olmalıdır")
    @Max(value = 500, message = "Kapasite en fazla 500 olabilir")
    private Integer capacity;

    private String scheduleJson;
}
