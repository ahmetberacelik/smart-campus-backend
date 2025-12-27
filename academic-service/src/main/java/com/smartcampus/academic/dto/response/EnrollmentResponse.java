package com.smartcampus.academic.dto.response;

import com.smartcampus.academic.entity.Enrollment;
import com.smartcampus.academic.entity.EnrollmentStatus;
import com.smartcampus.academic.entity.CourseSection;
import com.smartcampus.academic.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private Long sectionId;
    private String courseCode;
    private String courseName;
    private String sectionNumber;
    
    // YENİ ALANLAR - Section bilgileri
    private String semester;
    private Integer year;
    private String instructorName;
    private Integer capacity;
    private Integer enrolledCount;
    private Integer credits;
    
    private EnrollmentStatus status;
    private LocalDateTime enrollmentDate;
    private BigDecimal midtermGrade;
    private BigDecimal finalGrade;
    private BigDecimal homeworkGrade;
    private String letterGrade;
    private BigDecimal gradePoint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EnrollmentResponse from(Enrollment enrollment, String studentName) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment cannot be null");
        }
        
        CourseSection section = enrollment.getSection();
        if (section == null) {
            throw new IllegalArgumentException("Enrollment section cannot be null");
        }
        
        if (enrollment.getStudent() == null) {
            throw new IllegalArgumentException("Enrollment student cannot be null");
        }
        
        // Instructor name - null safety
        String instructorName = null;
        if (section.getInstructor() != null && section.getInstructor().getUser() != null) {
            User instructorUser = section.getInstructor().getUser();
            instructorName = instructorUser.getFirstName() + " " + instructorUser.getLastName();
        }
        
        // Course - null safety
        String courseCode = null;
        String courseName = null;
        Integer credits = null;
        if (section.getCourse() != null) {
            courseCode = section.getCourse().getCode();
            courseName = section.getCourse().getName();
            credits = section.getCourse().getCredits();
        }
        
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentNumber(enrollment.getStudent().getStudentNumber())
                .studentName(studentName != null ? studentName : "Bilinmeyen Öğrenci")
                .sectionId(section.getId())
                .courseCode(courseCode)
                .courseName(courseName)
                .sectionNumber(section.getSectionNumber())
                // YENİ ALANLAR
                .semester(section.getSemester())
                .year(section.getYear())
                .instructorName(instructorName)
                .capacity(section.getCapacity())
                .enrolledCount(section.getEnrolledCount())
                .credits(credits)
                // MEVCUT ALANLAR
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .midtermGrade(enrollment.getMidtermGrade())
                .finalGrade(enrollment.getFinalGrade())
                .homeworkGrade(enrollment.getHomeworkGrade())
                .letterGrade(enrollment.getLetterGrade())
                .gradePoint(enrollment.getGradePoint())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}
