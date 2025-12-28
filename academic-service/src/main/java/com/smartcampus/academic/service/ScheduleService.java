package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.request.CreateScheduleRequest;
import com.smartcampus.academic.dto.request.GenerateScheduleRequest;
import com.smartcampus.academic.dto.response.ScheduleResponse;
import com.smartcampus.academic.dto.response.GeneratedScheduleResponse;
import com.smartcampus.academic.dto.response.MyScheduleResponse;
import com.smartcampus.academic.entity.Schedule;

import java.time.LocalTime;
import java.util.List;

public interface ScheduleService {

    List<ScheduleResponse> getAllSchedules();

    ScheduleResponse getScheduleById(Long id);

    List<ScheduleResponse> getSchedulesBySection(Long sectionId);

    List<ScheduleResponse> getSchedulesByClassroom(Long classroomId);

    List<ScheduleResponse> getSchedulesByDay(Schedule.DayOfWeek dayOfWeek);

    ScheduleResponse createSchedule(CreateScheduleRequest request);

    ScheduleResponse updateSchedule(Long id, CreateScheduleRequest request);

    void deleteSchedule(Long id);

    boolean hasConflict(Long classroomId, Schedule.DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime, Long excludeId);

    /**
     * Otomatik program oluşturma (basit demo implementasyonu).
     * Şu aşamada seçilen bölümler için birkaç sabit zaman slotu üzerinden
     * alternatif programlar üretir ve sadece response döner, veritabanına yazmaz.
     */
    List<GeneratedScheduleResponse> generateSchedules(GenerateScheduleRequest request);

    /**
     * Öğrencinin/öğretim üyesinin ders programını getir.
     * Kullanıcının kayıtlı olduğu section'ların schedule'larını döndürür.
     */
    MyScheduleResponse getMySchedule(Long userId);
}
