package com.smartcampus.attendance.repository;

import com.smartcampus.attendance.entity.AttendanceSession;
import com.smartcampus.attendance.entity.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    List<AttendanceSession> findBySectionId(Long sectionId);

    List<AttendanceSession> findBySectionIdAndStatus(Long sectionId, SessionStatus status);

    List<AttendanceSession> findByInstructorId(Long instructorId);

    Page<AttendanceSession> findByInstructorId(Long instructorId, Pageable pageable);

    @Query("SELECT s FROM AttendanceSession s WHERE s.instructorId = :instructorId " +
            "AND (:sectionId IS NULL OR s.sectionId = :sectionId) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:startDate IS NULL OR s.sessionDate >= :startDate) " +
            "AND (:endDate IS NULL OR s.sessionDate <= :endDate)")
    Page<AttendanceSession> findByInstructorIdWithFilters(
            @Param("instructorId") Long instructorId,
            @Param("sectionId") Long sectionId,
            @Param("status") SessionStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT s FROM AttendanceSession s WHERE s.sectionId = :sectionId " +
            "AND (:startDate IS NULL OR s.sessionDate >= :startDate) " +
            "AND (:endDate IS NULL OR s.sessionDate <= :endDate)")
    List<AttendanceSession> findBySectionIdAndDateRange(
            @Param("sectionId") Long sectionId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<AttendanceSession> findBySectionIdAndSessionDateAndStatus(
            Long sectionId, LocalDate sessionDate, SessionStatus status);

    @Query("SELECT COUNT(s) FROM AttendanceSession s WHERE s.sectionId = :sectionId")
    Long countBySectionId(@Param("sectionId") Long sectionId);

    List<AttendanceSession> findBySectionIdIn(List<Long> sectionIds);

    @Query("SELECT s FROM AttendanceSession s WHERE s.sectionId IN :sectionIds " +
            "AND s.status = :status")
    List<AttendanceSession> findActiveSessions(
            @Param("sectionIds") List<Long> sectionIds,
            @Param("status") SessionStatus status);

    @Query("SELECT DISTINCT s.sectionId FROM AttendanceSession s WHERE s.id IN " +
            "(SELECT r.sessionId FROM AttendanceRecord r WHERE r.studentId = :studentId)")
    List<Long> findDistinctSectionIdsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT s FROM AttendanceSession s WHERE s.sectionId = :sectionId ORDER BY s.sessionDate DESC, s.startTime DESC")
    List<AttendanceSession> findBySectionIdOrderByDateDesc(@Param("sectionId") Long sectionId);
}