package com.smartcampus.academic.dto.response;

import com.smartcampus.academic.entity.CourseSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSectionResponse {

    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String sectionNumber;
    private String semester;
    private Integer year;
    private Long instructorId;
    private String instructorName;
    private Long classroomId;
    private String classroomName;
    private Integer capacity;
    private Integer enrolledCount;
    private Integer availableSlots;
    private String scheduleJson;
    private LocalDateTime createdAt;

    public static CourseSectionResponse from(CourseSection section, String instructorName) {
        return CourseSectionResponse.builder()
                .id(section.getId())
                .courseId(section.getCourse().getId())
                .courseCode(section.getCourse().getCode())
                .courseName(section.getCourse().getName())
                .sectionNumber(section.getSectionNumber())
                .semester(section.getSemester())
                .year(section.getYear())
                .instructorId(section.getInstructor().getId())
                .instructorName(instructorName)
                .classroomId(section.getClassroom() != null ? section.getClassroom().getId() : null)
                .classroomName(section.getClassroom() != null ? section.getClassroom().getFullName() : null)
                .capacity(section.getCapacity())
                .enrolledCount(section.getEnrolledCount())
                .availableSlots(section.getCapacity() - section.getEnrolledCount())
                .scheduleJson(section.getScheduleJson())
                .createdAt(section.getCreatedAt())
                .build();
    }
}