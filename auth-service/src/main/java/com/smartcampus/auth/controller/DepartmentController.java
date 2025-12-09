package com.smartcampus.auth.controller;

import com.smartcampus.auth.dto.response.ApiResponse;
import com.smartcampus.auth.dto.response.DepartmentResponse;
import com.smartcampus.auth.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Bölüm bilgileri")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Bölüm listesi", description = "Tüm bölümleri listeler")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Bölüm detayı", description = "Belirtilen bölümün detaylarını getirir")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @PathVariable Long id
    ) {
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Bölüm detayı (Kod ile)", description = "Bölüm koduna göre detayları getirir")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentByCode(
            @PathVariable String code
    ) {
        DepartmentResponse department = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(ApiResponse.success(department));
    }
}

