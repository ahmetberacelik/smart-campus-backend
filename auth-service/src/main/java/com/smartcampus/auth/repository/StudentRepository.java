package com.smartcampus.auth.repository;

import com.smartcampus.auth.entity.Student;
import com.smartcampus.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUser(User user);

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByStudentNumber(String studentNumber);

    boolean existsByStudentNumber(String studentNumber);
}

