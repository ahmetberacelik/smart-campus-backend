package com.smartcampus.academic.dto.response;

import com.smartcampus.academic.entity.Enrollment;
import com.smartcampus.academic.entity.EnrollmentStatus;
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
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentNumber(enrollment.getStudent().getStudentNumber())
                .studentName(studentName)
                .sectionId(enrollment.getSection().getId())
                .courseCode(enrollment.getSection().getCourse().getCode())
                .courseName(enrollment.getSection().getCourse().getName())
                .sectionNumber(enrollment.getSection().getSectionNumber())
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
