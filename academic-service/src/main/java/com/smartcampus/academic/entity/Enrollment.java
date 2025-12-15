package com.smartcampus.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;

    @Column(name = "midterm_grade", precision = 5, scale = 2)
    private BigDecimal midtermGrade;

    @Column(name = "final_grade", precision = 5, scale = 2)
    private BigDecimal finalGrade;

    @Column(name = "homework_grade", precision = 5, scale = 2)
    private BigDecimal homeworkGrade;

    @Column(name = "letter_grade", length = 2)
    private String letterGrade;

    @Column(name = "grade_point", precision = 3, scale = 2)
    private BigDecimal gradePoint;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        enrollmentDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculateTotalGrade() {
        if (midtermGrade == null && finalGrade == null && homeworkGrade == null) {
            return null;
        }
        BigDecimal midterm = midtermGrade != null ? midtermGrade : BigDecimal.ZERO;
        BigDecimal finalG = finalGrade != null ? finalGrade : BigDecimal.ZERO;
        BigDecimal homework = homeworkGrade != null ? homeworkGrade : BigDecimal.ZERO;
        
        return midterm.multiply(new BigDecimal("0.30"))
                .add(finalG.multiply(new BigDecimal("0.50")))
                .add(homework.multiply(new BigDecimal("0.20")));
    }
}
