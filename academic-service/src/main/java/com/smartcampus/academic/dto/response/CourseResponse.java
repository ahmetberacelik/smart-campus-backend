package com.smartcampus.academic.dto.response;

import com.smartcampus.academic.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer credits;
    private Integer ects;
    private Long departmentId;
    private String departmentName;
    private String syllabusUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse from(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .credits(course.getCredits())
                .ects(course.getEcts())
                .departmentId(course.getDepartment().getId())
                .departmentName(course.getDepartment().getName())
                .syllabusUrl(course.getSyllabusUrl())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
