package com.smartcampus.event.dto.request;

import com.smartcampus.event.entity.Event;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    
    @NotBlank(message = "Başlık zorunludur")
    @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
    private String title;
    
    private String description;
    
    @NotNull(message = "Kategori zorunludur")
    private Event.EventCategory category;
    
    @NotNull(message = "Etkinlik tarihi zorunludur")
    @FutureOrPresent(message = "Etkinlik tarihi geçmiş olamaz")
    private LocalDate eventDate;
    
    @NotNull(message = "Başlangıç saati zorunludur")
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    @NotBlank(message = "Konum zorunludur")
    @Size(max = 200, message = "Konum en fazla 200 karakter olabilir")
    private String location;
    
    @NotNull(message = "Kapasite zorunludur")
    @Min(value = 1, message = "Kapasite en az 1 olmalıdır")
    @Max(value = 10000, message = "Kapasite en fazla 10000 olabilir")
    private Integer capacity;
    
    private LocalDateTime registrationDeadline;
    
    private Boolean isPaid = false;
    
    @DecimalMin(value = "0.00", message = "Ücret negatif olamaz")
    private BigDecimal price = BigDecimal.ZERO;
    
    private String imageUrl;
}
