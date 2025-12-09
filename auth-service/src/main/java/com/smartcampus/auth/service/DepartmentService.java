package com.smartcampus.auth.service;

import com.smartcampus.auth.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse getDepartmentByCode(String code);
}

