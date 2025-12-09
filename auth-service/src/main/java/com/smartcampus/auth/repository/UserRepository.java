package com.smartcampus.auth.repository;

import com.smartcampus.auth.entity.Role;
import com.smartcampus.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.deletedAt IS NULL")
    Page<User> findAllActive(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.deletedAt IS NULL AND u.role = :role")
    Page<User> findAllByRole(@Param("role") Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.deletedAt IS NULL " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.deletedAt IS NULL " +
           "AND u.role = :role " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsersByRole(@Param("search") String search, @Param("role") Role role, Pageable pageable);
}

