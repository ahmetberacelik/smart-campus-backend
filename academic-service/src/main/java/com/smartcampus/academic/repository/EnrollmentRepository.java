package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.Enrollment;
import com.smartcampus.academic.entity.EnrollmentStatus;
import com.smartcampus.academic.entity.Semester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    List<Enrollment> findBySectionId(Long sectionId);

    Page<Enrollment> findBySectionId(Long sectionId, Pageable pageable);

    Optional<Enrollment> findByStudentIdAndSectionId(Long studentId, Long sectionId);

    boolean existsByStudentIdAndSectionId(Long studentId, Long sectionId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = :status")
    List<Enrollment> findByStudentIdAndStatus(@Param("studentId") Long studentId,
                                               @Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN e.section cs " +
            "WHERE e.student.id = :studentId AND cs.semester = :semester AND cs.year = :year")
    List<Enrollment> findByStudentIdAndSemesterAndYear(@Param("studentId") Long studentId,
                                                        @Param("semester") Semester semester,
                                                        @Param("year") Integer year);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN e.section cs " +
            "WHERE e.student.userId = :userId AND cs.semester = :semester AND cs.year = :year")
    List<Enrollment> findByStudentUserIdAndSemesterAndYear(@Param("userId") Long userId,
                                                            @Param("semester") Semester semester,
                                                            @Param("year") Integer year);

    @Query("SELECT e FROM Enrollment e WHERE e.section.id = :sectionId AND e.status = :status")
    List<Enrollment> findBySectionIdAndStatus(@Param("sectionId") Long sectionId,
                                               @Param("status") EnrollmentStatus status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.section.id = :sectionId AND e.status = 'ENROLLED'")
    long countEnrolledBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :userId AND e.status = 'ENROLLED'")
    List<Enrollment> findActiveEnrollmentsByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN e.section cs " +
            "WHERE cs.instructor.userId = :instructorUserId AND cs.semester = :semester AND cs.year = :year")
    List<Enrollment> findByInstructorUserIdAndSemesterAndYear(@Param("instructorUserId") Long instructorUserId,
                                                               @Param("semester") Semester semester,
                                                               @Param("year") Integer year);
}
