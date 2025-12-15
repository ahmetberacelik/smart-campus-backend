package com.smartcampus.attendance.repository;

import com.smartcampus.attendance.entity.AttendanceRecord;
import com.smartcampus.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findBySessionId(Long sessionId);

    List<AttendanceRecord> findByStudentId(Long studentId);

    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.sessionId IN :sessionIds AND r.studentId = :studentId")
    List<AttendanceRecord> findBySessionIdsAndStudentId(
            @Param("sessionIds") List<Long> sessionIds,
            @Param("studentId") Long studentId);

    @Query("SELECT COUNT(r) FROM AttendanceRecord r WHERE r.sessionId = :sessionId AND r.status = :status")
    Long countBySessionIdAndStatus(
            @Param("sessionId") Long sessionId,
            @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(r) FROM AttendanceRecord r WHERE r.sessionId IN " +
            "(SELECT s.id FROM AttendanceSession s WHERE s.sectionId = :sectionId) " +
            "AND r.studentId = :studentId AND r.status = :status")
    Long countByStudentAndSectionAndStatus(
            @Param("studentId") Long studentId,
            @Param("sectionId") Long sectionId,
            @Param("status") AttendanceStatus status);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.studentId = :studentId " +
            "ORDER BY r.createdAt DESC LIMIT 1")
    Optional<AttendanceRecord> findLastRecordByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.studentId = :studentId " +
            "AND r.checkInTime >= :since ORDER BY r.checkInTime DESC")
    List<AttendanceRecord> findRecentRecordsByStudentId(
            @Param("studentId") Long studentId,
            @Param("since") LocalDateTime since);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.sessionId IN :sessionIds")
    List<AttendanceRecord> findBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.isFlagged = true AND r.sessionId IN " +
            "(SELECT s.id FROM AttendanceSession s WHERE s.instructorId = :instructorId)")
    List<AttendanceRecord> findFlaggedRecordsByInstructor(@Param("instructorId") Long instructorId);

    @Query("SELECT DISTINCT r.studentId FROM AttendanceRecord r WHERE r.sessionId IN " +
            "(SELECT s.id FROM AttendanceSession s WHERE s.sectionId = :sectionId)")
    List<Long> findDistinctStudentIdsBySectionId(@Param("sectionId") Long sectionId);
}
