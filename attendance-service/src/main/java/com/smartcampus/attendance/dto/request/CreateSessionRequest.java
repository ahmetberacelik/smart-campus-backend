package com.smartcampus.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {

    @NotNull(message = "Section ID zorunludur")
    private Long sectionId;

    @Positive(message = "Geofence radius pozitif olmalıdır")
    private Integer geofenceRadius;

    @Positive(message = "Süre pozitif olmalıdır")
    private Integer durationMinutes;

    @NotNull(message = "Enlem zorunludur")
    private Double latitude;

    @NotNull(message = "Boylam zorunludur")
    private Double longitude;
}
