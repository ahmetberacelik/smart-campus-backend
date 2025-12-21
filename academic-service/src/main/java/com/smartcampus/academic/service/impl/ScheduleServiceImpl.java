package com.smartcampus.academic.service.impl;

import com.smartcampus.academic.dto.request.CreateScheduleRequest;
import com.smartcampus.academic.dto.response.ScheduleResponse;
import com.smartcampus.academic.entity.Classroom;
import com.smartcampus.academic.entity.CourseSection;
import com.smartcampus.academic.entity.Schedule;
import com.smartcampus.academic.repository.ClassroomRepository;
import com.smartcampus.academic.repository.CourseSectionRepository;
import com.smartcampus.academic.repository.ScheduleRepository;
import com.smartcampus.academic.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

        private final ScheduleRepository scheduleRepository;
        private final CourseSectionRepository sectionRepository;
        private final ClassroomRepository classroomRepository;

        @Override
        @Transactional(readOnly = true)
        public List<ScheduleResponse> getAllSchedules() {
                return scheduleRepository.findByIsActiveTrue()
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public ScheduleResponse getScheduleById(Long id) {
                Schedule schedule = scheduleRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Program bulunamadı: " + id));
                return mapToResponse(schedule);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ScheduleResponse> getSchedulesBySection(Long sectionId) {
                return scheduleRepository.findBySectionIdAndIsActiveTrue(sectionId)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ScheduleResponse> getSchedulesByClassroom(Long classroomId) {
                return scheduleRepository.findByClassroomIdAndIsActiveTrue(classroomId)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ScheduleResponse> getSchedulesByDay(Schedule.DayOfWeek dayOfWeek) {
                return scheduleRepository.findByDayOfWeekAndIsActiveTrue(dayOfWeek)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public ScheduleResponse createSchedule(CreateScheduleRequest request) {
                // Çakışma kontrolü
                if (hasConflict(request.getClassroomId(), request.getDayOfWeek(),
                                request.getStartTime(), request.getEndTime(), null)) {
                        throw new RuntimeException("Bu derslik ve saatte çakışma var!");
                }

                CourseSection section = sectionRepository.findById(request.getSectionId())
                                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı"));

                Classroom classroom = classroomRepository.findById(request.getClassroomId())
                                .orElseThrow(() -> new RuntimeException("Derslik bulunamadı"));

                Schedule schedule = Schedule.builder()
                                .section(section)
                                .dayOfWeek(request.getDayOfWeek())
                                .startTime(request.getStartTime())
                                .endTime(request.getEndTime())
                                .classroom(classroom)
                                .isActive(true)
                                .build();

                Schedule saved = scheduleRepository.save(schedule);
                log.info("Program oluşturuldu: section={}, classroom={}, day={}",
                                section.getSectionNumber(), classroom.getRoomNumber(), request.getDayOfWeek());

                return mapToResponse(saved);
        }

        @Override
        @Transactional
        public ScheduleResponse updateSchedule(Long id, CreateScheduleRequest request) {
                Schedule schedule = scheduleRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Program bulunamadı"));

                // Çakışma kontrolü (kendisi hariç)
                if (hasConflict(request.getClassroomId(), request.getDayOfWeek(),
                                request.getStartTime(), request.getEndTime(), id)) {
                        throw new RuntimeException("Bu derslik ve saatte çakışma var!");
                }

                CourseSection section = sectionRepository.findById(request.getSectionId())
                                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı"));

                Classroom classroom = classroomRepository.findById(request.getClassroomId())
                                .orElseThrow(() -> new RuntimeException("Derslik bulunamadı"));

                schedule.setSection(section);
                schedule.setDayOfWeek(request.getDayOfWeek());
                schedule.setStartTime(request.getStartTime());
                schedule.setEndTime(request.getEndTime());
                schedule.setClassroom(classroom);

                Schedule saved = scheduleRepository.save(schedule);
                log.info("Program güncellendi: id={}", id);

                return mapToResponse(saved);
        }

        @Override
        @Transactional
        public void deleteSchedule(Long id) {
                Schedule schedule = scheduleRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Program bulunamadı"));

                schedule.setIsActive(false);
                scheduleRepository.save(schedule);
                log.info("Program silindi (soft delete): id={}", id);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean hasConflict(Long classroomId, Schedule.DayOfWeek dayOfWeek,
                        LocalTime startTime, LocalTime endTime, Long excludeId) {
                List<Schedule> conflicts;
                if (excludeId != null) {
                        conflicts = scheduleRepository.findConflictingSchedules(
                                        classroomId, dayOfWeek, startTime, endTime, excludeId);
                } else {
                        conflicts = scheduleRepository.findConflictingSchedulesForNew(
                                        classroomId, dayOfWeek, startTime, endTime);
                }
                return !conflicts.isEmpty();
        }

        private ScheduleResponse mapToResponse(Schedule schedule) {
                CourseSection section = schedule.getSection();
                Classroom classroom = schedule.getClassroom();

                return ScheduleResponse.builder()
                                .id(schedule.getId())
                                .sectionId(section.getId())
                                .sectionCode(section.getSectionNumber())
                                .courseName(section.getCourse().getName())
                                .courseCode(section.getCourse().getCode())
                                .dayOfWeek(schedule.getDayOfWeek())
                                .startTime(schedule.getStartTime())
                                .endTime(schedule.getEndTime())
                                .classroomId(classroom.getId())
                                .classroomName(classroom.getBuilding() + " " + classroom.getRoomNumber())
                                .isActive(schedule.getIsActive())
                                .createdAt(schedule.getCreatedAt())
                                .build();
        }
}
