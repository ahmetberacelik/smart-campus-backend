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
    private Long courseDepartmentId;
    private String courseDepartmentName;
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
        // Null safety checks to prevent NullPointerException
        Long courseId = section.getCourse() != null ? section.getCourse().getId() : null;
        String courseCode = section.getCourse() != null ? section.getCourse().getCode() : null;
        String courseName = section.getCourse() != null ? section.getCourse().getName() : null;
        
        Long courseDepartmentId = null;
        String courseDepartmentName = null;
        if (section.getCourse() != null && section.getCourse().getDepartment() != null) {
            courseDepartmentId = section.getCourse().getDepartment().getId();
            courseDepartmentName = section.getCourse().getDepartment().getName();
        }
        
        Long instructorId = section.getInstructor() != null ? section.getInstructor().getId() : null;
        
        return CourseSectionResponse.builder()
                .id(section.getId())
                .courseId(courseId)
                .courseCode(courseCode)
                .courseName(courseName)
                .courseDepartmentId(courseDepartmentId)
                .courseDepartmentName(courseDepartmentName)
                .sectionNumber(section.getSectionNumber())
                .semester(section.getSemester())
                .year(section.getYear())
                .instructorId(instructorId)
                .instructorName(instructorName != null ? instructorName : "Unknown Instructor")
                .classroomId(section.getClassroom() != null ? section.getClassroom().getId() : null)
                .classroomName(section.getClassroom() != null ? section.getClassroom().getFullName() : null)
                .capacity(section.getCapacity())
                .enrolledCount(section.getEnrolledCount())
                .availableSlots(section.getCapacity() != null && section.getEnrolledCount() != null 
                    ? section.getCapacity() - section.getEnrolledCount() : null)
                .scheduleJson(section.getScheduleJson())
                .createdAt(section.getCreatedAt())
                .build();
    }
}