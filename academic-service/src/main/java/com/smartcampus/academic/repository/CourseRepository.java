package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    List<Course> findByDepartmentId(Long departmentId);

    Page<Course> findByDepartmentId(Long departmentId, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE " +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Course> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.department.id = :departmentId AND " +
            "(LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchByDepartmentAndKeyword(@Param("departmentId") Long departmentId,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);
}
