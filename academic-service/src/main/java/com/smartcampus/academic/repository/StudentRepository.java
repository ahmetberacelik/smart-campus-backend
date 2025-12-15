package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByStudentNumber(String studentNumber);

    boolean existsByUserId(Long userId);

    boolean existsByStudentNumber(String studentNumber);
}
