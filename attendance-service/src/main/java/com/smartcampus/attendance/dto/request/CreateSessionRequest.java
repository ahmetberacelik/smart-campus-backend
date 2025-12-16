package com.smartcampus.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // GPS koordinatları - Opsiyonel (sınıf konumundan veya varsayılandan alınır)
    private Double latitude;
    private Double longitude;

    // Frontend'den gelen tarih/saat bilgileri - Opsiyonel
    private LocalDate sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
