package com.smartcampus.academic.repository;

import com.smartcampus.academic.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // Bölüme göre programlar
    List<Schedule> findBySectionIdAndIsActiveTrue(Long sectionId);

    // Dersliğe göre programlar
    List<Schedule> findByClassroomIdAndIsActiveTrue(Long classroomId);

    // Güne göre programlar
    List<Schedule> findByDayOfWeekAndIsActiveTrue(Schedule.DayOfWeek dayOfWeek);

    // Derslik + gün + aktif
    List<Schedule> findByClassroomIdAndDayOfWeekAndIsActiveTrue(Long classroomId, Schedule.DayOfWeek dayOfWeek);

    // Çakışma kontrolü için
    @Query("SELECT s FROM Schedule s WHERE s.classroom.id = :classroomId AND s.dayOfWeek = :dayOfWeek " +
            "AND s.isActive = true AND s.id != :excludeId " +
            "AND NOT (s.endTime <= :startTime OR s.startTime >= :endTime)")
    List<Schedule> findConflictingSchedules(
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") Schedule.DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    // Yeni schedule için çakışma kontrolü (excludeId olmadan)
    @Query("SELECT s FROM Schedule s WHERE s.classroom.id = :classroomId AND s.dayOfWeek = :dayOfWeek " +
            "AND s.isActive = true " +
            "AND NOT (s.endTime <= :startTime OR s.startTime >= :endTime)")
    List<Schedule> findConflictingSchedulesForNew(
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") Schedule.DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    // Tüm aktif programlar
    List<Schedule> findByIsActiveTrue();
}
