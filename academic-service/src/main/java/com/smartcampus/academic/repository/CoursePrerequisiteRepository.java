package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.CoursePrerequisite;
import com.smartcampus.academic.entity.CoursePrerequisiteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, CoursePrerequisiteId> {

    @Query("SELECT cp.prerequisite.id FROM CoursePrerequisite cp WHERE cp.course.id = :courseId")
    Set<Long> findPrerequisiteIdsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT cp FROM CoursePrerequisite cp WHERE cp.course.id = :courseId")
    List<CoursePrerequisite> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT cp.prerequisite.code FROM CoursePrerequisite cp WHERE cp.course.id = :courseId")
    List<String> findPrerequisiteCodesByCourseId(@Param("courseId") Long courseId);

    boolean existsByCourseIdAndPrerequisiteId(Long courseId, Long prerequisiteId);
}
