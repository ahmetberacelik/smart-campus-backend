package com.smartcampus.event.repository;

import com.smartcampus.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Published ve gelecek tarihli etkinlikler
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND e.eventDate >= :today ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEvents(@Param("today") LocalDate today);
    
    // Sayfalı upcoming
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND e.eventDate >= :today ORDER BY e.eventDate ASC")
    Page<Event> findUpcomingEventsPaged(@Param("today") LocalDate today, Pageable pageable);
    
    // Kategoriye göre
    List<Event> findByCategoryAndStatusAndEventDateGreaterThanEqual(
            Event.EventCategory category, Event.EventStatus status, LocalDate date);
    
    // Organizatörün etkinlikleri
    List<Event> findByOrganizerIdOrderByEventDateDesc(Long organizerId);
    
    Page<Event> findByOrganizerIdOrderByEventDateDesc(Long organizerId, Pageable pageable);
    
    // Belirli tarihteki etkinlikler
    List<Event> findByEventDateAndStatus(LocalDate date, Event.EventStatus status);
    
    // Arama
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Event> searchEvents(@Param("query") String query, Pageable pageable);
    
    // Ücretli etkinlikler
    List<Event> findByIsPaidTrueAndStatusAndEventDateGreaterThanEqual(
            Event.EventStatus status, LocalDate date);
    
    // Status'a göre sayım
    long countByStatus(Event.EventStatus status);
}
