package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.UpdateGradeRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.EnrollmentResponse;
import com.smartcampus.academic.dto.response.TranscriptResponse;
import com.smartcampus.academic.security.CurrentUser;
import com.smartcampus.academic.security.CustomUserDetails;
import com.smartcampus.academic.service.GradeService;
import com.smartcampus.academic.service.TranscriptPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Not yönetimi controller'ı.
 * Öğrenci notları, transkript ve not girişi endpoint'lerini sağlar.
 */
@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final TranscriptPdfService transcriptPdfService;

    /**
     * Öğrencinin notlarını getir
     * GET /api/v1/grades/my-grades
     */
    @GetMapping("/my-grades")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyGrades(
            @CurrentUser CustomUserDetails userDetails) {
        List<EnrollmentResponse> grades = gradeService.getStudentGrades(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    /**
     * Transkript JSON formatında
     * GET /api/v1/grades/transcript
     */
    @GetMapping("/transcript")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<TranscriptResponse>> getTranscript(
            @CurrentUser CustomUserDetails userDetails) {
        TranscriptResponse transcript = gradeService.getTranscript(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(transcript));
    }

    /**
     * Transkript PDF formatında
     * GET /api/v1/grades/transcript/pdf
     */
    @GetMapping("/transcript/pdf")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<byte[]> getTranscriptPdf(
            @CurrentUser CustomUserDetails userDetails) {
        // Önce transcript verisini al
        TranscriptResponse transcript = gradeService.getTranscript(userDetails.getId());
        // PDF oluştur
        byte[] pdfBytes = transcriptPdfService.generateTranscriptPdf(transcript);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "transkript_" + transcript.getStudentNumber() + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Not girişi (Öğretim görevlisi)
     * POST /api/v1/grades
     */
    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enterGrade(
            @CurrentUser CustomUserDetails userDetails,
            @Valid @RequestBody UpdateGradeRequest request) {
        EnrollmentResponse enrollment = gradeService.enterGrade(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Not başarıyla girildi", enrollment));
    }

    /**
     * Toplu not girişi (Öğretim görevlisi)
     * POST /api/v1/grades/batch
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> enterGradesBatch(
            @CurrentUser CustomUserDetails userDetails,
            @Valid @RequestBody List<UpdateGradeRequest> requests) {
        List<EnrollmentResponse> results = gradeService.enterGradesBatch(userDetails.getId(), requests);
        return ResponseEntity.ok(ApiResponse.success("Notlar başarıyla girildi", results));
    }

    /**
     * Belirli bir öğrencinin notlarını getir (Admin/Faculty)
     * GET /api/v1/grades/student/{userId}
     */
    @GetMapping("/student/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getStudentGrades(
            @PathVariable Long userId) {
        List<EnrollmentResponse> grades = gradeService.getStudentGrades(userId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    /**
     * Belirli bir öğrencinin transkriptini getir (Admin/Faculty)
     * GET /api/v1/grades/transcript/{userId}
     */
    @GetMapping("/transcript/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<TranscriptResponse>> getStudentTranscript(
            @PathVariable Long userId) {
        TranscriptResponse transcript = gradeService.getTranscript(userId);
        return ResponseEntity.ok(ApiResponse.success(transcript));
    }

    /**
     * Belirli bir öğrencinin transkript PDF'ini getir (Admin/Faculty)
     * GET /api/v1/grades/transcript/{userId}/pdf
     */
    @GetMapping("/transcript/{userId}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<byte[]> getStudentTranscriptPdf(@PathVariable Long userId) {
        TranscriptResponse transcript = gradeService.getTranscript(userId);
        byte[] pdfBytes = transcriptPdfService.generateTranscriptPdf(transcript);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "transkript_" + transcript.getStudentNumber() + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}

