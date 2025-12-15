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
public class UpdateGradeRequest {

    @DecimalMin(value = "0.0", message = "Vize notu 0'dan küçük olamaz")
    @DecimalMax(value = "100.0", message = "Vize notu 100'den büyük olamaz")
    private BigDecimal midtermGrade;

    @DecimalMin(value = "0.0", message = "Final notu 0'dan küçük olamaz")
    @DecimalMax(value = "100.0", message = "Final notu 100'den büyük olamaz")
    private BigDecimal finalGrade;

    @DecimalMin(value = "0.0", message = "Ödev notu 0'dan küçük olamaz")
    @DecimalMax(value = "100.0", message = "Ödev notu 100'den büyük olamaz")
    private BigDecimal homeworkGrade;
}
