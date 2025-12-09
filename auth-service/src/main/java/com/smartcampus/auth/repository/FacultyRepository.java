package com.smartcampus.auth.repository;

import com.smartcampus.auth.entity.Faculty;
import com.smartcampus.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByUser(User user);

    Optional<Faculty> findByUserId(Long userId);

    Optional<Faculty> findByEmployeeNumber(String employeeNumber);

    boolean existsByEmployeeNumber(String employeeNumber);
}

