package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByUserId(Long userId);

    Optional<Faculty> findByEmployeeNumber(String employeeNumber);

    List<Faculty> findByDepartmentId(Long departmentId);

    boolean existsByUserId(Long userId);

    boolean existsByEmployeeNumber(String employeeNumber);
}
