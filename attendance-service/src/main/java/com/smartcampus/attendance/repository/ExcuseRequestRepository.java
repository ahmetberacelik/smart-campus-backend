package com.smartcampus.attendance.repository;

import com.smartcampus.attendance.entity.ExcuseRequest;
import com.smartcampus.attendance.entity.ExcuseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExcuseRequestRepository extends JpaRepository<ExcuseRequest, Long> {

    List<ExcuseRequest> findByStudentId(Long studentId);

    Page<ExcuseRequest> findByStudentId(Long studentId, Pageable pageable);

    Optional<ExcuseRequest> findBySessionId(Long sessionId);

    Optional<ExcuseRequest> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    List<ExcuseRequest> findByStatus(ExcuseStatus status);

    @Query("SELECT e FROM ExcuseRequest e WHERE e.sessionId IN " +
            "(SELECT s.id FROM AttendanceSession s WHERE s.instructorId = :instructorId)")
    Page<ExcuseRequest> findByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    @Query("SELECT e FROM ExcuseRequest e WHERE e.sessionId IN " +
            "(SELECT s.id FROM AttendanceSession s WHERE s.instructorId = :instructorId " +
            "AND (:sectionId IS NULL OR s.sectionId = :sectionId)) " +
            "AND (:status IS NULL OR e.status = :status)")
    Page<ExcuseRequest> findByInstructorIdWithFilters(
            @Param("instructorId") Long instructorId,
            @Param("sectionId") Long sectionId,
            @Param("status") ExcuseStatus status,
            Pageable pageable);

    @Query("SELECT COUNT(e) FROM ExcuseRequest e WHERE e.studentId = :studentId AND e.status = :status")
    Long countByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") ExcuseStatus status);

    @Query("SELECT e FROM ExcuseRequest e WHERE e.sessionId IN " +
            "(SELECT s.id FROM AttendanceSession s WHERE s.sectionId = :sectionId)")
    List<ExcuseRequest> findBySectionId(@Param("sectionId") Long sectionId);

    boolean existsBySessionIdAndStudentId(Long sessionId, Long studentId);
}
