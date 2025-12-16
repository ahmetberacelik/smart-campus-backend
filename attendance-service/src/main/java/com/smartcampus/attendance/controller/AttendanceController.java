package com.smartcampus.attendance.controller;

import com.smartcampus.attendance.dto.request.CheckInQrRequest;
import com.smartcampus.attendance.dto.request.CheckInRequest;
import com.smartcampus.attendance.dto.request.CreateSessionRequest;
import com.smartcampus.attendance.dto.response.*;
import com.smartcampus.attendance.entity.SessionStatus;
import com.smartcampus.attendance.security.CurrentUser;
import com.smartcampus.attendance.security.CustomUserDetails;
import com.smartcampus.attendance.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/sessions")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @CurrentUser CustomUserDetails userDetails,
            @Valid @RequestBody CreateSessionRequest request) {
        SessionResponse response = attendanceService.createSession(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Yoklama oturumu başlatıldı", response));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(@PathVariable Long id) {
        SessionResponse response = attendanceService.getSession(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/sessions/{id}/close")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<SessionResponse>> closeSession(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long id) {
        SessionResponse response = attendanceService.closeSession(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Yoklama oturumu kapatıldı", response));
    }

    @GetMapping("/sessions/my-sessions")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<PageResponse<SessionResponse>>> getMySessions(
            @CurrentUser CustomUserDetails userDetails,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sessionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<SessionResponse> response = attendanceService.getMySessions(
                userDetails.getId(), sectionId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/report/{sectionId}")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<AttendanceReportResponse>> getAttendanceReport(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AttendanceReportResponse response = attendanceService.getAttendanceReport(
                userDetails.getId(), sectionId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/sessions/{id}/checkin")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CheckInRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        CheckInResponse response = attendanceService.checkIn(userDetails.getId(), id, request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Yoklama başarıyla verildi", response));
    }

    @PostMapping("/sessions/{id}/checkin-qr")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkInWithQr(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CheckInQrRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        CheckInResponse response = attendanceService.checkInWithQr(userDetails.getId(), id, request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Yoklama başarıyla verildi", response));
    }

    @GetMapping("/my-attendance")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<MyAttendanceResponse>>> getMyAttendance(
            @CurrentUser CustomUserDetails userDetails,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer year) {
        List<MyAttendanceResponse> response = attendanceService.getMyAttendance(userDetails.getId(), semester, year);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Öğrencinin kayıtlı olduğu derslerdeki aktif yoklama oturumlarını getirir
     */
    @GetMapping("/active-sessions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getActiveSessionsForStudent(
            @CurrentUser CustomUserDetails userDetails) {
        List<SessionResponse> response = attendanceService.getActiveSessionsForStudent(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/sessions/{id}/refresh-qr")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<SessionResponse>> refreshQrCode(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long id) {
        SessionResponse response = attendanceService.refreshQrCode(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("QR kod yenilendi", response));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
