package com.smartcampus.event.dto.response;

import com.smartcampus.event.entity.Event;
import com.smartcampus.event.entity.EventRegistration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private Event.EventCategory eventCategory;
    private String eventLocation;
    private Long userId;
    private LocalDateTime registrationDate;
    private String qrCode;
    private String qrCodeImage;
    private Boolean checkedIn;
    private LocalDateTime checkedInAt;
    private EventRegistration.RegistrationStatus status;
    private Integer waitlistPosition;
    private Boolean isCancellable;
    private LocalDateTime createdAt;
}
