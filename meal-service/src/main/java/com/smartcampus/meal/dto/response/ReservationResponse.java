package com.smartcampus.meal.dto.response;

import com.smartcampus.meal.entity.MealMenu;
import com.smartcampus.meal.entity.MealReservation;
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
public class ReservationResponse {
    private Long id;
    private Long userId;
    private Long menuId;
    private Long cafeteriaId;
    private String cafeteriaName;
    private LocalDate reservationDate;
    private MealMenu.MealType mealType;
    private BigDecimal amount;
    private String qrCode;
    private String qrCodeImage; // Base64 encoded QR image
    private Boolean isScholarshipUsed;
    private MealReservation.ReservationStatus status;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private Boolean isCancellable;
    
    // Menu details
    private String menuItemsJson;
}
