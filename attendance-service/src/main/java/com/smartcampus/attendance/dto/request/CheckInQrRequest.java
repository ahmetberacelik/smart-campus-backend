package com.smartcampus.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    // Konum opsiyonel - QR kod zaten güvenli olduğu için konum kontrolü yapılmayabilir
    private Double latitude;

    private Double longitude;

    private Double accuracy;

    private String deviceInfo;

    private Boolean isMockLocation;
}
