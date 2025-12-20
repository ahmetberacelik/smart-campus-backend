package com.smartcampus.event.controller;

import com.smartcampus.event.dto.request.EventRequest;
import com.smartcampus.event.dto.response.ApiResponse;
import com.smartcampus.event.dto.response.EventResponse;
import com.smartcampus.event.entity.Event;
import com.smartcampus.event.security.CustomUserDetails;
import com.smartcampus.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Yaklaşan etkinlikleri listele (Public)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getEvents(Pageable pageable) {
        Page<EventResponse> events = eventService.getUpcomingEventsPaged(pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Yaklaşan etkinlikler - basit liste (Public)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents() {
        List<EventResponse> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Etkinlik detayı (Public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    /**
     * Kategoriye göre etkinlikler (Public)
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsByCategory(
            @PathVariable Event.EventCategory category) {
        List<EventResponse> events = eventService.getEventsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Etkinlik ara (Public)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> searchEvents(
            @RequestParam String q,
            Pageable pageable) {
        Page<EventResponse> events = eventService.searchEvents(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Benim etkinliklerim (Organizatör)
     */
    @GetMapping("/my-events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getMyEvents(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<EventResponse> events = eventService.getMyEvents(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Etkinlik oluştur (Faculty/Admin)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EventRequest request) {
        EventResponse event = eventService.createEvent(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Etkinlik oluşturuldu", event));
    }

    /**
     * Etkinlik güncelle (Organizatör)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EventRequest request) {
        EventResponse event = eventService.updateEvent(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Etkinlik güncellendi", event));
    }

    /**
     * Etkinlik sil (Admin veya Organizatör - sadece DRAFT)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.deleteEvent(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Etkinlik silindi", null));
    }

    /**
     * Etkinlik yayınla
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> publishEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.publishEvent(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Etkinlik yayınlandı", null));
    }

    /**
     * Etkinlik iptal et
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.cancelEvent(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Etkinlik iptal edildi", null));
    }
}
