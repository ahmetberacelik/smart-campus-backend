package com.smartcampus.event.service.impl;

import com.smartcampus.event.dto.request.EventRequest;
import com.smartcampus.event.dto.response.EventResponse;
import com.smartcampus.event.entity.Event;
import com.smartcampus.event.repository.EventRepository;
import com.smartcampus.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getUpcomingEventsPaged(Pageable pageable) {
        return eventRepository.findUpcomingEventsPaged(LocalDate.now(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı: " + id));
        return mapToResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByCategory(Event.EventCategory category) {
        return eventRepository.findByCategoryAndStatusAndEventDateGreaterThanEqual(
                        category, Event.EventStatus.PUBLISHED, LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(String query, Pageable pageable) {
        return eventRepository.searchEvents(query, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public EventResponse createEvent(Long organizerId, EventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .registeredCount(0)
                .registrationDeadline(request.getRegistrationDeadline())
                .isPaid(request.getIsPaid() != null ? request.getIsPaid() : false)
                .price(request.getPrice())
                .organizerId(organizerId)
                .imageUrl(request.getImageUrl())
                .status(Event.EventStatus.DRAFT)
                .build();

        Event saved = eventRepository.save(event);
        log.info("Etkinlik oluşturuldu: id={}, title={}, organizer={}", saved.getId(), saved.getTitle(), organizerId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, Long userId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Bu etkinliği düzenleme yetkiniz yok");
        }

        if (event.getStatus() == Event.EventStatus.COMPLETED || event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new RuntimeException("Tamamlanmış veya iptal edilmiş etkinlik düzenlenemez");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setEventDate(request.getEventDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setCapacity(request.getCapacity());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setIsPaid(request.getIsPaid());
        event.setPrice(request.getPrice());
        event.setImageUrl(request.getImageUrl());

        Event saved = eventRepository.save(event);
        log.info("Etkinlik güncellendi: id={}", eventId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        // Sadece ADMIN silebilir veya organizatör kendi DRAFT etkinliğini silebilir
        if (!event.getOrganizerId().equals(userId) && event.getStatus() != Event.EventStatus.DRAFT) {
            throw new RuntimeException("Bu etkinliği silme yetkiniz yok");
        }

        eventRepository.delete(event);
        log.info("Etkinlik silindi: id={}", eventId);
    }

    @Override
    @Transactional
    public void publishEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Bu etkinliği yayınlama yetkiniz yok");
        }

        if (event.getStatus() != Event.EventStatus.DRAFT) {
            throw new RuntimeException("Sadece taslak etkinlikler yayınlanabilir");
        }

        event.setStatus(Event.EventStatus.PUBLISHED);
        eventRepository.save(event);
        log.info("Etkinlik yayınlandı: id={}", eventId);
    }

    @Override
    @Transactional
    public void cancelEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Bu etkinliği iptal etme yetkiniz yok");
        }

        if (event.getStatus() == Event.EventStatus.COMPLETED) {
            throw new RuntimeException("Tamamlanmış etkinlik iptal edilemez");
        }

        event.setStatus(Event.EventStatus.CANCELLED);
        eventRepository.save(event);
        log.info("Etkinlik iptal edildi: id={}", eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getMyEvents(Long organizerId) {
        return eventRepository.findByOrganizerIdOrderByEventDateDesc(organizerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getMyEventsPaged(Long organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerIdOrderByEventDateDesc(organizerId, pageable)
                .map(this::mapToResponse);
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .registeredCount(event.getRegisteredCount())
                .availableSpots(event.getAvailableSpots())
                .registrationDeadline(event.getRegistrationDeadline())
                .isPaid(event.getIsPaid())
                .price(event.getPrice())
                .organizerId(event.getOrganizerId())
                .imageUrl(event.getImageUrl())
                .status(event.getStatus())
                .isRegistrationOpen(event.isRegistrationOpen())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
