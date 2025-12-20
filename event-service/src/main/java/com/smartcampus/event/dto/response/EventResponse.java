package com.smartcampus.event.dto.response;

import com.smartcampus.event.entity.Event;
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
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private Event.EventCategory category;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private Integer capacity;
    private Integer registeredCount;
    private Integer availableSpots;
    private LocalDateTime registrationDeadline;
    private Boolean isPaid;
    private BigDecimal price;
    private Long organizerId;
    private String imageUrl;
    private Event.EventStatus status;
    private Boolean isRegistrationOpen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
