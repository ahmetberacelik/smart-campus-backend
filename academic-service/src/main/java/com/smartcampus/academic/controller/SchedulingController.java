package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.GenerateScheduleRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.GeneratedScheduleResponse;
import com.smartcampus.academic.dto.response.MyScheduleResponse;
import com.smartcampus.academic.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Course scheduling ile ilgili ek endpoint'ler.
 *
 * Not: Bu controller, frontend'in kullandığı
 * - POST /api/v1/scheduling/generate
 * - GET /api/v1/scheduling/my-schedule
 * endpoint'lerini sağlamak için eklendi.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/scheduling")
@RequiredArgsConstructor
public class SchedulingController {

    private final ScheduleService scheduleService;

    /**
     * Öğrencinin ders programını getir.
     * Frontend'deki /my-schedule sayfası bu endpoint'i çağırır.
     */
    @GetMapping("/my-schedule")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<ApiResponse<MyScheduleResponse>> getMySchedule(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Getting schedule for user: {}", userId);

        MyScheduleResponse schedule = scheduleService.getMySchedule(userId);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

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
