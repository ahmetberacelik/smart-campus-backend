package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    // userId field'ı kullanarak query (user_id column'u)
    @Query("SELECT f FROM Faculty f WHERE f.userId = :userId")
    Optional<Faculty> findByUserId(@Param("userId") Long userId);

    Optional<Faculty> findByEmployeeNumber(String employeeNumber);

    List<Faculty> findByDepartmentId(Long departmentId);

    // userId field'ı kullanarak query (user_id column'u)
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Faculty f WHERE f.userId = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    boolean existsByEmployeeNumber(String employeeNumber);
}
