package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.EnrollRequest;
import com.smartcampus.academic.dto.request.UpdateGradeRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.EnrollmentResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.dto.response.TranscriptResponse;
import com.smartcampus.academic.entity.Semester;
import com.smartcampus.academic.security.CurrentUser;
import com.smartcampus.academic.security.CustomUserDetails;
import com.smartcampus.academic.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @CurrentUser CustomUserDetails userDetails,
            @Valid @RequestBody EnrollRequest request) {
        EnrollmentResponse enrollment = enrollmentService.enrollStudent(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Derse kayıt başarılı", enrollment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(@PathVariable Long id) {
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(ApiResponse.success(enrollment));
    }

    @GetMapping("/my-enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(@CurrentUser CustomUserDetails userDetails) {
        List<EnrollmentResponse> enrollments = enrollmentService.getStudentEnrollments(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/my-enrollments/semester")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollmentsBySemester(
            @CurrentUser CustomUserDetails userDetails,
            @RequestParam Semester semester,
            @RequestParam Integer year) {
        List<EnrollmentResponse> enrollments = enrollmentService.getStudentEnrollmentsBySemester(
                userDetails.getId(), semester, year);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/my-enrollments/active")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyActiveEnrollments(@CurrentUser CustomUserDetails userDetails) {
        List<EnrollmentResponse> enrollments = enrollmentService.getActiveEnrollments(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getSectionEnrollments(
            @PathVariable Long sectionId,
            @PageableDefault(size = 50) Pageable pageable) {
        PageResponse<EnrollmentResponse> enrollments = enrollmentService.getSectionEnrollments(sectionId, pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @PutMapping("/{enrollmentId}/grade")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateGrade(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody UpdateGradeRequest request) {
        EnrollmentResponse enrollment = enrollmentService.updateGrade(enrollmentId, request);
        return ResponseEntity.ok(ApiResponse.success("Not başarıyla güncellendi", enrollment));
    }

    @DeleteMapping("/drop/{sectionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> dropEnrollment(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long sectionId) {
        enrollmentService.dropEnrollment(userDetails.getId(), sectionId);
        return ResponseEntity.ok(ApiResponse.success("Ders bırakma başarılı"));
    }

    @GetMapping("/transcript")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<TranscriptResponse>> getMyTranscript(@CurrentUser CustomUserDetails userDetails) {
        TranscriptResponse transcript = enrollmentService.getStudentTranscript(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(transcript));
    }

    @GetMapping("/transcript/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<TranscriptResponse>> getStudentTranscript(@PathVariable Long userId) {
        TranscriptResponse transcript = enrollmentService.getStudentTranscript(userId);
        return ResponseEntity.ok(ApiResponse.success(transcript));
    }

    @GetMapping("/check/{sectionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Boolean>> checkEnrollment(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long sectionId) {
        boolean enrolled = enrollmentService.isStudentEnrolled(userDetails.getId(), sectionId);
        return ResponseEntity.ok(ApiResponse.success(enrolled));
    }
}
