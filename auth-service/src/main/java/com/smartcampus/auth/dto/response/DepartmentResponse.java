package com.smartcampus.auth.dto.response;

import com.smartcampus.auth.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private Long id;
    private String name;
    private String code;
    private String facultyName;

    public static DepartmentResponse fromDepartment(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .facultyName(department.getFacultyName())
                .build();
    }
}

