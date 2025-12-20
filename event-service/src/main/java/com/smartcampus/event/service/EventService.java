package com.smartcampus.event.service;

import com.smartcampus.event.dto.request.EventRequest;
import com.smartcampus.event.dto.response.EventResponse;
import com.smartcampus.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {
    
    // Public - okuma
    List<EventResponse> getUpcomingEvents();
    Page<EventResponse> getUpcomingEventsPaged(Pageable pageable);
    EventResponse getEventById(Long id);
    List<EventResponse> getEventsByCategory(Event.EventCategory category);
    Page<EventResponse> searchEvents(String query, Pageable pageable);
    
    // Authenticated - organizatör işlemleri
    EventResponse createEvent(Long organizerId, EventRequest request);
    EventResponse updateEvent(Long eventId, Long userId, EventRequest request);
    void deleteEvent(Long eventId, Long userId);
    void publishEvent(Long eventId, Long userId);
    void cancelEvent(Long eventId, Long userId);
    
    // Organizatörün etkinlikleri
    List<EventResponse> getMyEvents(Long organizerId);
    Page<EventResponse> getMyEventsPaged(Long organizerId, Pageable pageable);
}
