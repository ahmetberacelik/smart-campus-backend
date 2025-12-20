package com.smartcampus.event.service.impl;

import com.smartcampus.event.dto.response.RegistrationResponse;
import com.smartcampus.event.entity.Event;
import com.smartcampus.event.entity.EventRegistration;
import com.smartcampus.event.repository.EventRegistrationRepository;
import com.smartcampus.event.repository.EventRepository;
import com.smartcampus.event.service.QRCodeService;
import com.smartcampus.event.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final QRCodeService qrCodeService;

    @Override
    @Transactional
    public RegistrationResponse registerForEvent(Long eventId, Long userId, String customFieldsJson) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        // Kayıt açık mı kontrol
        if (!event.isRegistrationOpen()) {
            throw new RuntimeException("Kayıt dönemi kapalı");
        }

        // Zaten kayıtlı mı kontrol
        if (registrationRepository.existsByEventIdAndUserIdAndStatusNot(
                eventId, userId, EventRegistration.RegistrationStatus.CANCELLED)) {
            throw new RuntimeException("Bu etkinliğe zaten kayıtlısınız");
        }

        // TODO: Ücretli etkinlik için wallet kontrolü ve ödeme
        // if (event.getIsPaid()) { ... }

        String qrCode = qrCodeService.generateUniqueCode();
        EventRegistration.RegistrationStatus status;
        Integer waitlistPosition = null;

        // Kapasite kontrolü
        if (event.hasAvailableCapacity()) {
            status = EventRegistration.RegistrationStatus.REGISTERED;
            event.incrementRegisteredCount();
            eventRepository.save(event);
        } else {
            // Waitlist'e ekle
            status = EventRegistration.RegistrationStatus.WAITLIST;
            waitlistPosition = registrationRepository.findMaxWaitlistPosition(eventId).orElse(0) + 1;
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .userId(userId)
                .qrCode(qrCode)
                .status(status)
                .waitlistPosition(waitlistPosition)
                .customFieldsJson(customFieldsJson)
                .checkedIn(false)
                .build();

        EventRegistration saved = registrationRepository.save(registration);
        log.info("Etkinlik kaydı oluşturuldu: eventId={}, userId={}, status={}", eventId, userId, status);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long eventId, Long userId) {
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new RuntimeException("Kayıt bulunamadı"));

        if (!registration.isCancellable()) {
            throw new RuntimeException("Bu kayıt iptal edilemez");
        }

        boolean wasRegistered = registration.getStatus() == EventRegistration.RegistrationStatus.REGISTERED;
        registration.setStatus(EventRegistration.RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        // Eğer kayıtlıysa, etkinlik sayısını düşür ve waitlist'ten birini kaydet
        if (wasRegistered) {
            Event event = registration.getEvent();
            event.decrementRegisteredCount();
            eventRepository.save(event);

            // Waitlist'ten ilk kişiyi kayıtlıya al
            promoteFromWaitlist(eventId);
        }

        log.info("Kayıt iptal edildi: eventId={}, userId={}", eventId, userId);
    }

    @Transactional
    protected void promoteFromWaitlist(Long eventId) {
        List<EventRegistration> waitlist = registrationRepository.findWaitlistByEventId(eventId);
        if (!waitlist.isEmpty()) {
            EventRegistration next = waitlist.get(0);
            next.setStatus(EventRegistration.RegistrationStatus.REGISTERED);
            next.setWaitlistPosition(null);
            registrationRepository.save(next);

            Event event = eventRepository.findById(eventId).orElseThrow();
            event.incrementRegisteredCount();
            eventRepository.save(event);

            // Diğer waitlist sıralarını güncelle
            for (int i = 1; i < waitlist.size(); i++) {
                EventRegistration r = waitlist.get(i);
                r.setWaitlistPosition(i);
                registrationRepository.save(r);
            }

            log.info("Waitlist'ten kayıt alındı: eventId={}, userId={}", eventId, next.getUserId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations(Long userId) {
        return registrationRepository.findByUserIdWithEvent(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponse> getMyRegistrationsPaged(Long userId, Pageable pageable) {
        return registrationRepository.findByUserIdOrderByRegistrationDateDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getEventRegistrations(Long eventId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("Bu etkinliğin katılımcılarını görme yetkiniz yok");
        }

        return registrationRepository.findByEventIdOrderByRegistrationDateAsc(eventId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponse> getEventRegistrationsPaged(Long eventId, Long organizerId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("Bu etkinliğin katılımcılarını görme yetkiniz yok");
        }

        return registrationRepository.findByEventIdOrderByRegistrationDateAsc(eventId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public RegistrationResponse checkIn(String qrCode, Long staffUserId) {
        EventRegistration registration = registrationRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Kayıt bulunamadı"));

        if (!registration.canCheckIn()) {
            throw new RuntimeException("Bu kayıt ile giriş yapılamaz. Durum: " + registration.getStatus());
        }

        registration.checkIn();
        registrationRepository.save(registration);

        log.info("Check-in yapıldı: qrCode={}, staffUserId={}", qrCode, staffUserId);
        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationByQrCode(String qrCode) {
        EventRegistration registration = registrationRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Kayıt bulunamadı"));
        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public long getRegistrationCount(Long eventId) {
        return registrationRepository.countByEventIdAndStatus(eventId, EventRegistration.RegistrationStatus.REGISTERED);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCheckedInCount(Long eventId) {
        return registrationRepository.countByEventIdAndCheckedInTrue(eventId);
    }

    private RegistrationResponse mapToResponse(EventRegistration registration) {
        Event event = registration.getEvent();
        String qrImage = qrCodeService.generateQRCodeImage(registration.getQrCode(), 200, 200);

        return RegistrationResponse.builder()
                .id(registration.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .eventDate(event.getEventDate())
                .eventCategory(event.getCategory())
                .eventLocation(event.getLocation())
                .userId(registration.getUserId())
                .registrationDate(registration.getRegistrationDate())
                .qrCode(registration.getQrCode())
                .qrCodeImage(qrImage)
                .checkedIn(registration.getCheckedIn())
                .checkedInAt(registration.getCheckedInAt())
                .status(registration.getStatus())
                .waitlistPosition(registration.getWaitlistPosition())
                .isCancellable(registration.isCancellable())
                .createdAt(registration.getCreatedAt())
                .build();
    }
}
