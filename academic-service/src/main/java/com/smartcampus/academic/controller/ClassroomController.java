package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.CreateClassroomRequest;
import com.smartcampus.academic.dto.request.UpdateClassroomRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.ClassroomResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.service.ClassroomService;
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
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassroomResponse>> createClassroom(@Valid @RequestBody CreateClassroomRequest request) {
        ClassroomResponse classroom = classroomService.createClassroom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Derslik başarıyla oluşturuldu", classroom));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> getClassroomById(@PathVariable Long id) {
        ClassroomResponse classroom = classroomService.getClassroomById(id);
        return ResponseEntity.ok(ApiResponse.success(classroom));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClassroomResponse>>> getAllClassrooms(
            @PageableDefault(size = 20, sort = "building", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<ClassroomResponse> classrooms = classroomService.getAllClassrooms(pageable);
        return ResponseEntity.ok(ApiResponse.success(classrooms));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PageResponse<ClassroomResponse>>> getActiveClassrooms(
            @PageableDefault(size = 20, sort = "building", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<ClassroomResponse> classrooms = classroomService.getActiveClassrooms(pageable);
        return ResponseEntity.ok(ApiResponse.success(classrooms));
    }

    @GetMapping("/building/{building}")
    public ResponseEntity<ApiResponse<PageResponse<ClassroomResponse>>> getClassroomsByBuilding(
            @PathVariable String building,
            @PageableDefault(size = 20, sort = "roomNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<ClassroomResponse> classrooms = classroomService.getClassroomsByBuilding(building, pageable);
        return ResponseEntity.ok(ApiResponse.success(classrooms));
    }

    @GetMapping("/buildings")
    public ResponseEntity<ApiResponse<List<String>>> getBuildings() {
        List<String> buildings = classroomService.getBuildings();
        return ResponseEntity.ok(ApiResponse.success(buildings));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ClassroomResponse>>> searchClassrooms(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<ClassroomResponse> classrooms = classroomService.searchClassrooms(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(classrooms));
    }

    @GetMapping("/capacity/{minCapacity}")
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getClassroomsByCapacity(@PathVariable Integer minCapacity) {
        List<ClassroomResponse> classrooms = classroomService.getClassroomsByMinCapacity(minCapacity);
        return ResponseEntity.ok(ApiResponse.success(classrooms));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassroomResponse>> updateClassroom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassroomRequest request) {
        ClassroomResponse classroom = classroomService.updateClassroom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Derslik başarıyla güncellendi", classroom));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.ok(ApiResponse.success("Derslik başarıyla silindi"));
    }
}
