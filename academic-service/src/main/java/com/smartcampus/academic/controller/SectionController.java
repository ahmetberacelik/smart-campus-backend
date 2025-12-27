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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.smartcampus.academic.repository.FacultyRepository;
import com.smartcampus.academic.entity.Faculty;
import com.smartcampus.academic.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final FacultyRepository facultyRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> createSection(@Valid @RequestBody CreateSectionRequest request) {
        try {
            System.out.println("🔍 SectionController: POST /sections called with instructorId: " + request.getInstructorId());
            CourseSectionResponse section = sectionService.createSection(request);
            System.out.println("✅ SectionController: Section created successfully");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Section created successfully", section));
        } catch (ResourceNotFoundException e) {
            System.err.println("❌ SectionController: Resource not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "RESOURCE_NOT_FOUND"));
        } catch (Exception e) {
            System.err.println("❌ SectionController: Unexpected error in createSection: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ders atanırken bir hata oluştu: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> getAllSections(
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) Long instructorUserId) {
        List<CourseSectionResponse> sections;
        if (instructorUserId != null) {
            // User ID ile filtreleme (frontend'den User ID geliyor)
            System.out.println("🔍 SectionController: Filtering by instructorUserId: " + instructorUserId);
            sections = sectionService.getSectionsByInstructorUserId(instructorUserId);
            System.out.println("✅ SectionController: Found " + sections.size() + " sections for instructorUserId: " + instructorUserId);
        } else if (instructorId != null) {
            // Faculty ID ile filtreleme
            System.out.println("🔍 SectionController: Filtering by instructorId: " + instructorId);
            sections = sectionService.getSectionsByInstructor(instructorId);
            System.out.println("✅ SectionController: Found " + sections.size() + " sections for instructorId: " + instructorId);
        } else {
            System.out.println("⚠️ SectionController: No filter parameter, returning all sections");
            sections = sectionService.getAllSections();
            System.out.println("✅ SectionController: Found " + sections.size() + " total sections");
        }
        return ResponseEntity.ok(ApiResponse.success(sections));
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

    @GetMapping("/instructor/{instructorUserId}")
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> getSectionsByInstructorUserId(@PathVariable Long instructorUserId) {
        try {
            System.out.println("🔍 SectionController: GET /instructor/{instructorUserId} called with: " + instructorUserId);
            List<CourseSectionResponse> sections = sectionService.getSectionsByInstructorUserId(instructorUserId);
            System.out.println("✅ SectionController: Found " + sections.size() + " sections for instructorUserId: " + instructorUserId);
            return ResponseEntity.ok(ApiResponse.success(sections));
        } catch (Exception e) {
            System.err.println("❌ SectionController: Error in getSectionsByInstructorUserId for userId " + instructorUserId + ": " + e.getMessage());
            e.printStackTrace();
            // Hata durumunda boş liste döndür, 500 hatası verme
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
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

    // DEBUG: Faculty kayıtlarını kontrol et
    @GetMapping("/debug/faculty-check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkFaculty(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // Tüm Faculty kayıtlarını getir
        List<Faculty> allFaculty = facultyRepository.findAll();
        result.put("totalFacultyRecords", allFaculty.size());
        
        // User ID ile eşleşen Faculty kaydını bul
        var facultyOpt = facultyRepository.findByUserId(userId);
        result.put("foundByUserId", facultyOpt.isPresent());
        
        if (facultyOpt.isPresent()) {
            Faculty faculty = facultyOpt.get();
            result.put("facultyId", faculty.getId());
            result.put("userId", faculty.getUserId());
            result.put("employeeNumber", faculty.getEmployeeNumber());
            result.put("title", faculty.getTitle());
        } else {
            result.put("message", "User ID " + userId + " için Faculty kaydı bulunamadı");
            
            // Tüm Faculty kayıtlarını listele
            List<Map<String, Object>> allFacultyList = allFaculty.stream()
                .map(f -> {
                    Map<String, Object> facultyInfo = new HashMap<>();
                    facultyInfo.put("facultyId", f.getId());
                    facultyInfo.put("userId", f.getUserId());
                    facultyInfo.put("employeeNumber", f.getEmployeeNumber());
                    facultyInfo.put("title", f.getTitle());
                    return facultyInfo;
                })
                .toList();
            result.put("allFacultyRecords", allFacultyList);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Faculty check completed", result));
    }
}