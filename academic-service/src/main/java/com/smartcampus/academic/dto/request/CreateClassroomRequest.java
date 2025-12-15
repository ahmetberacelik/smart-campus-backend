package com.smartcampus.academic.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassroomRequest {

    @NotBlank(message = "Bina adı zorunludur")
    @Size(max = 50, message = "Bina adı en fazla 50 karakter olabilir")
    private String building;

    @NotBlank(message = "Oda numarası zorunludur")
    @Size(max = 20, message = "Oda numarası en fazla 20 karakter olabilir")
    private String roomNumber;

    @NotNull(message = "Kapasite zorunludur")
    @Min(value = 1, message = "Kapasite en az 1 olmalıdır")
    private Integer capacity;

    @NotNull(message = "Enlem zorunludur")
    @DecimalMin(value = "-90.0", message = "Enlem -90 ile 90 arasında olmalıdır")
    @DecimalMax(value = "90.0", message = "Enlem -90 ile 90 arasında olmalıdır")
    private BigDecimal latitude;

    @NotNull(message = "Boylam zorunludur")
    @DecimalMin(value = "-180.0", message = "Boylam -180 ile 180 arasında olmalıdır")
    @DecimalMax(value = "180.0", message = "Boylam -180 ile 180 arasında olmalıdır")
    private BigDecimal longitude;

    private String featuresJson;
}
