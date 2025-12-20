package com.smartcampus.event.repository;

import com.smartcampus.event.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    
    // QR kod ile bul
    Optional<EventRegistration> findByQrCode(String qrCode);
    
    // Kullanıcının etkinlik kaydı
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
    
    // Kullanıcının aktif kaydı var mı
    boolean existsByEventIdAndUserIdAndStatusNot(Long eventId, Long userId, EventRegistration.RegistrationStatus status);
    
    // Etkinliğin tüm kayıtları
    List<EventRegistration> findByEventIdOrderByRegistrationDateAsc(Long eventId);
    
    Page<EventRegistration> findByEventIdOrderByRegistrationDateAsc(Long eventId, Pageable pageable);
    
    // Etkinliğin aktif kayıtları
    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId AND r.status IN ('REGISTERED', 'ATTENDED')")
    List<EventRegistration> findActiveRegistrations(@Param("eventId") Long eventId);
    
    // Kullanıcının tüm kayıtları
    @Query("SELECT r FROM EventRegistration r JOIN FETCH r.event WHERE r.userId = :userId ORDER BY r.event.eventDate DESC")
    List<EventRegistration> findByUserIdWithEvent(@Param("userId") Long userId);
    
    Page<EventRegistration> findByUserIdOrderByRegistrationDateDesc(Long userId, Pageable pageable);
    
    // Waitlist sırası ile bul
    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = 'WAITLIST' ORDER BY r.waitlistPosition ASC")
    List<EventRegistration> findWaitlistByEventId(@Param("eventId") Long eventId);
    
    // En düşük waitlist sırasını bul
    @Query("SELECT MIN(r.waitlistPosition) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = 'WAITLIST'")
    Optional<Integer> findMinWaitlistPosition(@Param("eventId") Long eventId);
    
    // En yüksek waitlist sırasını bul
    @Query("SELECT MAX(r.waitlistPosition) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = 'WAITLIST'")
    Optional<Integer> findMaxWaitlistPosition(@Param("eventId") Long eventId);
    
    // Etkinliğe kayıtlı sayısı
    long countByEventIdAndStatus(Long eventId, EventRegistration.RegistrationStatus status);
    
    // Check-in yapılmış kayıtlar
    long countByEventIdAndCheckedInTrue(Long eventId);
}
