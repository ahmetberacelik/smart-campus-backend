package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.GenerateScheduleRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.GeneratedScheduleResponse;
import com.smartcampus.academic.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Course scheduling ile ilgili ek endpoint'ler.
 *
 * Not: Bu controller, frontend'in kullandığı
 *  - POST /api/v1/scheduling/generate
 * endpoint'ini sağlamak için eklendi.
 * Şu anki implementasyon basit bir demo algoritması kullanıyor ve
 * veritabanına kayıt yapmıyor; mevcut ScheduleController davranışını
 * etkilemez.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/scheduling")
@RequiredArgsConstructor
public class SchedulingController {

    private final ScheduleService scheduleService;

    /**
     * Otomatik program oluşturma (Admin).
     * Frontend'deki /admin/scheduling/generate sayfası bu endpoint'i çağırır.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GeneratedScheduleResponse>>> generateSchedule(
            @Valid @RequestBody GenerateScheduleRequest request) {

        log.info("Received schedule generation request: semester={}, year={}, sections={}",
                request.getSemester(), request.getYear(), request.getSectionIds());

        List<GeneratedScheduleResponse> alternatives = scheduleService.generateSchedules(request);
        return ResponseEntity.ok(ApiResponse.success("Program alternatifleri oluşturuldu", alternatives));
    }
}


