package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.CreateSectionRequest;
import com.smartcampus.academic.dto.request.UpdateSectionRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.CourseSectionResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.security.CurrentUser;
import com.smartcampus.academic.security.CustomUserDetails;
import com.smartcampus.academic.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> createSection(@Valid @RequestBody CreateSectionRequest request) {
        CourseSectionResponse section = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Section created successfully", section));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> getSectionById(@PathVariable Long id) {
        CourseSectionResponse section = sectionService.getSectionById(id);
        return ResponseEntity.ok(ApiResponse.success(section));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> getSectionsByCourse(@PathVariable Long courseId) {
        List<CourseSectionResponse> sections = sectionService.getSectionsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(sections));
    }

    @GetMapping("/semester")
    public ResponseEntity<ApiResponse<PageResponse<CourseSectionResponse>>> getSectionsBySemester(
            @RequestParam String semester,
            @RequestParam Integer year,
            @PageableDefault(size = 20, sort = "course.code", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<CourseSectionResponse> sections = sectionService.getSectionsBySemesterAndYear(semester, year, pageable);
        return ResponseEntity.ok(ApiResponse.success(sections));
    }

    @GetMapping("/semester/list")
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> getSectionsBySemesterList(
            @RequestParam String semester,
            @RequestParam Integer year) {
        List<CourseSectionResponse> sections = sectionService.getSectionsBySemesterAndYear(semester, year);
        return ResponseEntity.ok(ApiResponse.success(sections));
    }

    @GetMapping("/my-sections")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> getMyInstructorSections(
            @CurrentUser CustomUserDetails userDetails,
            @RequestParam String semester,
            @RequestParam Integer year) {
        List<CourseSectionResponse> sections = sectionService.getInstructorSections(userDetails.getId(), semester, year);
        return ResponseEntity.ok(ApiResponse.success(sections));
    }

    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableYears() {
        List<Integer> years = sectionService.getAvailableYears();
        return ResponseEntity.ok(ApiResponse.success(years));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSectionRequest request) {
        CourseSectionResponse section = sectionService.updateSection(id, request);
        return ResponseEntity.ok(ApiResponse.success("Section updated successfully", section));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok(ApiResponse.success("Section deleted successfully"));
    }
}