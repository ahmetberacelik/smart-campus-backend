package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.CreateScheduleRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.ScheduleResponse;
import com.smartcampus.academic.entity.Schedule;
import com.smartcampus.academic.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * Tüm programları listele
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAllSchedules() {
        List<ScheduleResponse> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    /**
     * Program detayı
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleById(@PathVariable Long id) {
        ScheduleResponse schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    /**
     * Bölüme göre programlar
     */
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesBySection(
            @PathVariable Long sectionId) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesBySection(sectionId);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    /**
     * Dersliğe göre programlar
     */
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByClassroom(
            @PathVariable Long classroomId) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByClassroom(classroomId);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    /**
     * Güne göre programlar
     */
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByDay(
            @PathVariable Schedule.DayOfWeek dayOfWeek) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDay(dayOfWeek);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    /**
     * Program oluştur (Admin)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request) {
        ScheduleResponse schedule = scheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Program oluşturuldu", schedule));
    }

    /**
     * Program güncelle (Admin)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody CreateScheduleRequest request) {
        ScheduleResponse schedule = scheduleService.updateSchedule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Program güncellendi", schedule));
    }

    /**
     * Program sil (Admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Program silindi", null));
    }

    /**
     * Çakışma kontrolü
     */
    @PostMapping("/check-conflict")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkConflict(
            @RequestParam Long classroomId,
            @RequestParam Schedule.DayOfWeek dayOfWeek,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,
            @RequestParam(required = false) Long excludeId) {
        boolean hasConflict = scheduleService.hasConflict(classroomId, dayOfWeek, startTime, endTime, excludeId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("hasConflict", hasConflict)));
    }
}
