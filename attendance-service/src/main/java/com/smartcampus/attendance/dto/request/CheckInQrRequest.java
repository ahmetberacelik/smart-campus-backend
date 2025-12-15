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
public class CheckInQrRequest {

    @NotBlank(message = "QR kod zorunludur")
    private String qrCode;

    @NotNull(message = "Enlem zorunludur")
    private Double latitude;

    @NotNull(message = "Boylam zorunludur")
    private Double longitude;

    private Double accuracy;

    private String deviceInfo;

    private Boolean isMockLocation;
}
