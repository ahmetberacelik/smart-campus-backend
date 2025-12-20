package com.smartcampus.meal.dto.request;

import com.smartcampus.meal.entity.MealMenu;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    
    @NotNull(message = "Menü ID zorunludur")
    private Long menuId;
    
    @NotNull(message = "Yemekhane ID zorunludur")
    private Long cafeteriaId;
    
    @NotNull(message = "Rezervasyon tarihi zorunludur")
    private LocalDate reservationDate;
    
    @NotNull(message = "Öğün tipi zorunludur")
    private MealMenu.MealType mealType;
    
    // Burs kullanmak istiyor mu?
    private Boolean useScholarship = false;
}
