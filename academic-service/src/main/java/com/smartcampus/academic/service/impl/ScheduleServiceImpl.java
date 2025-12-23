package com.smartcampus.academic.service.impl;

import com.smartcampus.academic.dto.request.CreateScheduleRequest;
import com.smartcampus.academic.dto.request.GenerateScheduleRequest;
import com.smartcampus.academic.dto.response.ScheduleResponse;
import com.smartcampus.academic.dto.response.GeneratedScheduleResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

        private final ScheduleRepository scheduleRepository;
        private final CourseSectionRepository sectionRepository;
        private final ClassroomRepository classroomRepository;

        // generateSchedules için basit in-memory id üretici
        private final AtomicLong generatedIdSequence = new AtomicLong(1L);

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

        @Override
        @Transactional(readOnly = true)
        public List<GeneratedScheduleResponse> generateSchedules(GenerateScheduleRequest request) {
                // TODO: Tam CSP algoritması ileride eklenecek. Şimdilik basit bir demo implementasyonu.
                log.info("Generating demo schedules for semester={} year={} sections={}",
                        request.getSemester(), request.getYear(), request.getSectionIds());

                if (request.getSectionIds() == null || request.getSectionIds().isEmpty()) {
                        return List.of();
                }

                // Seçilen bölümleri ve ilişkili dersleri getir
                List<CourseSection> sections = sectionRepository.findAllById(request.getSectionIds());
                if (sections.isEmpty()) {
                        return List.of();
                }

                // Her alternatif için kullanılacak basit zaman slotları
                LocalTime[][] timeSlots = new LocalTime[][]{
                        {LocalTime.of(9, 0), LocalTime.of(10, 30)},
                        {LocalTime.of(11, 0), LocalTime.of(12, 30)},
                        {LocalTime.of(14, 0), LocalTime.of(15, 30)},
                        {LocalTime.of(15, 45), LocalTime.of(17, 15)}
                };

                Schedule.DayOfWeek[] days = new Schedule.DayOfWeek[]{
                        Schedule.DayOfWeek.MONDAY,
                        Schedule.DayOfWeek.TUESDAY,
                        Schedule.DayOfWeek.WEDNESDAY,
                        Schedule.DayOfWeek.THURSDAY,
                        Schedule.DayOfWeek.FRIDAY
                };

                // Her biri farklı gün/saat kombinasyonları deneyen 2 basit alternatif üretelim
                List<GeneratedScheduleResponse> results = new ArrayList<>();

                for (int alt = 0; alt < 2; alt++) {
                        List<GeneratedScheduleResponse.GeneratedScheduleEntry> entries = new ArrayList<>();

                        int slotIndex = alt; // alternatifler arasında slot kaydırma
                        int dayIndex = 0;

                        for (CourseSection section : sections) {
                                LocalTime start = timeSlots[slotIndex % timeSlots.length][0];
                                LocalTime end = timeSlots[slotIndex % timeSlots.length][1];
                                Schedule.DayOfWeek day = days[dayIndex % days.length];

                                String classroomName = "Derslik henüz atanmadı";
                                // Varsa mevcut schedule kayıtlarından dersliğin adını tahmin etmeye çalış
                                List<Schedule> existing = scheduleRepository
                                        .findBySectionIdAndIsActiveTrue(section.getId());
                                if (!existing.isEmpty()) {
                                        Classroom classroom = existing.get(0).getClassroom();
                                        classroomName = classroom.getBuilding() + " " + classroom.getRoomNumber();
                                }

                                entries.add(GeneratedScheduleResponse.GeneratedScheduleEntry.builder()
                                        .sectionId(section.getId())
                                        .courseCode(section.getCourse().getCode())
                                        .courseName(section.getCourse().getName())
                                        .dayOfWeek(day)
                                        .startTime(start)
                                        .endTime(end)
                                        .classroomName(classroomName)
                                        .build());

                                slotIndex++;
                                if (slotIndex % timeSlots.length == 0) {
                                        dayIndex++;
                                }
                        }

                        // Basit bir skor hesaplaması: daha az "conflict" varsayıyoruz
                        int conflicts = 0;
                        double score = 100.0 - (alt * 5); // alternatiflere göre ufak fark

                        GeneratedScheduleResponse generated = GeneratedScheduleResponse.builder()
                                .id(generatedIdSequence.getAndIncrement())
                                .semester(request.getSemester())
                                .year(request.getYear())
                                .entries(entries)
                                .conflicts(conflicts)
                                .score(score)
                                .build();

                        results.add(generated);
                }

                return results;
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
