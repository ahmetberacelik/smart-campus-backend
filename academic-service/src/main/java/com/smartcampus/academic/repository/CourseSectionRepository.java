package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.CourseSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findByCourseId(Long courseId);

    List<CourseSection> findByInstructorId(Long instructorId);

    Page<CourseSection> findByInstructorId(Long instructorId, Pageable pageable);

    List<CourseSection> findBySemesterAndYear(String semester, Integer year);

    Page<CourseSection> findBySemesterAndYear(String semester, Integer year, Pageable pageable);

    @Query("SELECT cs FROM CourseSection cs WHERE cs.course.id = :courseId " +
            "AND cs.semester = :semester AND cs.year = :year")
    List<CourseSection> findByCourseAndSemesterAndYear(@Param("courseId") Long courseId,
                                                        @Param("semester") String semester,
                                                        @Param("year") Integer year);

    @Query("SELECT cs FROM CourseSection cs WHERE cs.course.id = :courseId " +
            "AND cs.sectionNumber = :sectionNumber AND cs.semester = :semester AND cs.year = :year")
    Optional<CourseSection> findByCourseAndSectionAndSemesterAndYear(
            @Param("courseId") Long courseId,
            @Param("sectionNumber") String sectionNumber,
            @Param("semester") String semester,
            @Param("year") Integer year);

    @Query("SELECT cs FROM CourseSection cs WHERE cs.instructor.userId = :userId " +
            "AND cs.semester = :semester AND cs.year = :year")
    List<CourseSection> findByInstructorUserIdAndSemesterAndYear(@Param("userId") Long userId,
                                                                  @Param("semester") String semester,
                                                                  @Param("year") Integer year);

    @Query("SELECT cs FROM CourseSection cs " +
            "JOIN cs.course c " +
            "WHERE c.department.id = :departmentId AND cs.semester = :semester AND cs.year = :year")
    List<CourseSection> findByDepartmentAndSemesterAndYear(@Param("departmentId") Long departmentId,
                                                            @Param("semester") String semester,
                                                            @Param("year") Integer year);

    @Query("SELECT DISTINCT cs.year FROM CourseSection cs ORDER BY cs.year DESC")
    List<Integer> findDistinctYears();
}