package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.CreateCourseRequest;
import com.smartcampus.academic.dto.request.UpdateCourseRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.CourseResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.service.CourseService;
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
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse course = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ders başarıyla oluşturuldu", course));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseByCode(@PathVariable String code) {
        CourseResponse course = courseService.getCourseByCode(code);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getAllCourses(
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<CourseResponse> courses = courseService.getAllCourses(pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCoursesByDepartment(
            @PathVariable Long departmentId,
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<CourseResponse> courses = courseService.getCoursesByDepartment(departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/department/{departmentId}/list")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByDepartmentList(@PathVariable Long departmentId) {
        List<CourseResponse> courses = courseService.getCoursesByDepartmentList(departmentId);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<CourseResponse> courses = courseService.searchCourses(keyword, departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        CourseResponse course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Ders başarıyla güncellendi", course));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Ders başarıyla silindi"));
    }
}
