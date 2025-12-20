package com.smartcampus.meal.dto.response;

import com.smartcampus.meal.entity.MealMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {
    private Long id;
    private Long cafeteriaId;
    private String cafeteriaName;
    private LocalDate menuDate;
    private MealMenu.MealType mealType;
    private String itemsJson;
    private String nutritionJson;
    private BigDecimal price;
    private Boolean isVegan;
    private Boolean isVegetarian;
    private Boolean isPublished;
    private LocalDateTime createdAt;
}
