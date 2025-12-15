package com.smartcampus.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcuseRequestDto {

    @NotNull(message = "Yoklama oturumu ID zorunludur")
    private Long sessionId;

    @NotBlank(message = "Mazeret nedeni zorunludur")
    private String reason;
}
