package com.smartcampus.auth.service.impl;

import com.smartcampus.auth.dto.response.DepartmentResponse;
import com.smartcampus.auth.entity.Department;
import com.smartcampus.auth.exception.ResourceNotFoundException;
import com.smartcampus.auth.repository.DepartmentRepository;
import com.smartcampus.auth.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::fromDepartment)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bölüm", "id", id));
        return DepartmentResponse.fromDepartment(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Bölüm", "code", code));
        return DepartmentResponse.fromDepartment(department);
    }
}

