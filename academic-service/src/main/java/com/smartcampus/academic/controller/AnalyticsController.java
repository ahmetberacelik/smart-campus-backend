package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.response.*;
import com.smartcampus.academic.service.AnalyticsService;
import com.smartcampus.academic.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Sistem analitiği ve raporlama (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ExportService exportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard istatistikleri", description = "Genel sistem istatistiklerini getirir")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/academic")
    @Operation(summary = "Akademik istatistikler", description = "GPA, not dağılımı, başarı oranları")
    public ResponseEntity<ApiResponse<AcademicStatsResponse>> getAcademicStats() {
        AcademicStatsResponse stats = analyticsService.getAcademicStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/attendance")
    @Operation(summary = "Yoklama istatistikleri", description = "Yoklama oranları, devamsızlık uyarıları")
    public ResponseEntity<ApiResponse<AttendanceStatsResponse>> getAttendanceStats() {
        AttendanceStatsResponse stats = analyticsService.getAttendanceStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/meals")
    @Operation(summary = "Yemek istatistikleri", description = "Yemek rezervasyonu ve kullanım verileri")
    public ResponseEntity<ApiResponse<MealStatsResponse>> getMealStats() {
        MealStatsResponse stats = analyticsService.getMealStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/events")
    @Operation(summary = "Etkinlik istatistikleri", description = "Etkinlik ve kayıt verileri")
    public ResponseEntity<ApiResponse<EventStatsResponse>> getEventStats() {
        EventStatsResponse stats = analyticsService.getEventStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ========== Export Endpoints ==========

    @GetMapping("/export/dashboard/excel")
    @Operation(summary = "Dashboard Excel export", description = "Dashboard verilerini Excel formatında indir")
    public ResponseEntity<byte[]> exportDashboardExcel() {
        byte[] data = exportService.exportDashboardToExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard_report.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/export/dashboard/csv")
    @Operation(summary = "Dashboard CSV export", description = "Dashboard verilerini CSV formatında indir")
    public ResponseEntity<byte[]> exportDashboardCsv() {
        byte[] data = exportService.exportDashboardToCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @GetMapping("/export/dashboard/pdf")
    @Operation(summary = "Dashboard PDF export", description = "Dashboard verilerini PDF formatında indir")
    public ResponseEntity<byte[]> exportDashboardPdf() {
        byte[] data = exportService.exportDashboardToPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/export/academic/excel")
    @Operation(summary = "Academic Excel export", description = "Akademik verileri Excel formatında indir")
    public ResponseEntity<byte[]> exportAcademicExcel() {
        byte[] data = exportService.exportAcademicToExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=academic_report.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
