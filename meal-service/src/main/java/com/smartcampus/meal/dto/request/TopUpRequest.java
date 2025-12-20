package com.smartcampus.meal.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequest {
    
    @NotNull(message = "Tutar zorunludur")
    @DecimalMin(value = "1.0", message = "Minimum yükleme tutarı 1 TL'dir")
    private BigDecimal amount;
    
    private String paymentMethod; // STRIPE, CREDIT_CARD, etc.
}
