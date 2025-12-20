package com.smartcampus.event.service;

import com.smartcampus.event.dto.response.RegistrationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RegistrationService {
    
    // Kullanıcı işlemleri
    RegistrationResponse registerForEvent(Long eventId, Long userId, String customFieldsJson);
    void cancelRegistration(Long eventId, Long userId);
    List<RegistrationResponse> getMyRegistrations(Long userId);
    Page<RegistrationResponse> getMyRegistrationsPaged(Long userId, Pageable pageable);
    
    // Organizatör işlemleri
    List<RegistrationResponse> getEventRegistrations(Long eventId, Long organizerId);
    Page<RegistrationResponse> getEventRegistrationsPaged(Long eventId, Long organizerId, Pageable pageable);
    
    // Check-in işlemleri
    RegistrationResponse checkIn(String qrCode, Long staffUserId);
    RegistrationResponse getRegistrationByQrCode(String qrCode);
    
    // İstatistikler
    long getRegistrationCount(Long eventId);
    long getCheckedInCount(Long eventId);
}
