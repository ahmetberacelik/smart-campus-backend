package com.smartcampus.attendance.controller;

import com.smartcampus.attendance.dto.request.ExcuseRequestDto;
import com.smartcampus.attendance.dto.request.ReviewExcuseRequest;
import com.smartcampus.attendance.dto.response.ApiResponse;
import com.smartcampus.attendance.dto.response.ExcuseRequestResponse;
import com.smartcampus.attendance.dto.response.PageResponse;
import com.smartcampus.attendance.entity.ExcuseStatus;
import com.smartcampus.attendance.security.CurrentUser;
import com.smartcampus.attendance.security.CustomUserDetails;
import com.smartcampus.attendance.service.ExcuseRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/attendance/excuse-requests")
@RequiredArgsConstructor
public class ExcuseRequestController {

    private final ExcuseRequestService excuseRequestService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ExcuseRequestResponse>> createExcuseRequest(
            @CurrentUser CustomUserDetails userDetails,
            @RequestParam Long sessionId,
            @RequestParam String reason,
            @RequestParam(required = false) MultipartFile document) {

        ExcuseRequestDto request = ExcuseRequestDto.builder()
                .sessionId(sessionId)
                .reason(reason)
                .build();

        ExcuseRequestResponse response = excuseRequestService.createExcuseRequest(
                userDetails.getId(), request, document);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mazeret başvurusu alındı", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<PageResponse<ExcuseRequestResponse>>> getExcuseRequests(
            @CurrentUser CustomUserDetails userDetails,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) ExcuseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<ExcuseRequestResponse> response = excuseRequestService.getExcuseRequestsForFaculty(
                userDetails.getId(), sectionId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<ExcuseRequestResponse>>> getMyExcuseRequests(
            @CurrentUser CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<ExcuseRequestResponse> response = excuseRequestService.getMyExcuseRequests(
                userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<ExcuseRequestResponse>> approveExcuseRequest(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) ReviewExcuseRequest request) {

        if (request == null) {
            request = new ReviewExcuseRequest();
        }

        ExcuseRequestResponse response = excuseRequestService.approveExcuseRequest(
                userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Mazeret onaylandı", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<ExcuseRequestResponse>> rejectExcuseRequest(
            @CurrentUser CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) ReviewExcuseRequest request) {

        if (request == null) {
            request = new ReviewExcuseRequest();
        }

        ExcuseRequestResponse response = excuseRequestService.rejectExcuseRequest(
                userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Mazeret reddedildi", response));
    }
}
