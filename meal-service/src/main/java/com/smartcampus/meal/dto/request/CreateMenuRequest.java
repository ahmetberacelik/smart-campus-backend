package com.smartcampus.meal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {
    
    @NotNull(message = "Yemekhane ID zorunludur")
    private Long cafeteriaId;
    
    @NotNull(message = "Menü tarihi zorunludur")
    private LocalDate menuDate;
    
    @NotNull(message = "Öğün tipi zorunludur")
    private String mealType; // "LUNCH" veya "DINNER"
    
    @NotNull(message = "Menü öğeleri zorunludur")
    private String itemsJson; // JSON string
    
    private String nutritionJson; // JSON string (opsiyonel)
    
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;
    
    @Builder.Default
    private Boolean isVegan = false;
    
    @Builder.Default
    private Boolean isVegetarian = false;
}

