package com.smartcampus.academic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    @NotNull(message = "Derslik ID zorunludur")
    private Long classroomId;

    @NotNull(message = "Rezervasyon tarihi zorunludur")
    private LocalDate reservationDate;

    @NotNull(message = "Başlangıç saati zorunludur")
    private LocalTime startTime;

    @NotNull(message = "Bitiş saati zorunludur")
    private LocalTime endTime;

    @NotBlank(message = "Kullanım amacı zorunludur")
    private String purpose;

    private String notes;
}
