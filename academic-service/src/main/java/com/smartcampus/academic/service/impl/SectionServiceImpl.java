package com.smartcampus.academic.service.impl;

import com.smartcampus.academic.dto.request.CreateSectionRequest;
import com.smartcampus.academic.dto.request.UpdateSectionRequest;
import com.smartcampus.academic.dto.response.CourseSectionResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.entity.Classroom;
import com.smartcampus.academic.entity.Course;
import com.smartcampus.academic.entity.CourseSection;
import com.smartcampus.academic.entity.Faculty;
import com.smartcampus.academic.entity.User;
import com.smartcampus.academic.exception.ConflictException;
import com.smartcampus.academic.exception.ResourceNotFoundException;
import com.smartcampus.academic.repository.ClassroomRepository;
import com.smartcampus.academic.repository.CourseRepository;
import com.smartcampus.academic.repository.CourseSectionRepository;
import com.smartcampus.academic.repository.FacultyRepository;
import com.smartcampus.academic.repository.UserRepository;
import com.smartcampus.academic.service.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final CourseSectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CourseSectionResponse createSection(CreateSectionRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        Faculty instructor = facultyRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor", request.getInstructorId()));

        if (sectionRepository.findByCourseAndSectionAndSemesterAndYear(
                request.getCourseId(), request.getSectionNumber(),
                request.getSemester(), request.getYear()).isPresent()) {
            throw new ConflictException("Section already exists for this semester");
        }

        Classroom classroom = null;
        if (request.getClassroomId() != null) {
            classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom", request.getClassroomId()));
        }

        CourseSection section = CourseSection.builder()
                .course(course)
                .sectionNumber(request.getSectionNumber())
                .semester(request.getSemester())
                .year(request.getYear())
                .instructor(instructor)
                .classroom(classroom)
                .capacity(request.getCapacity() != null ? request.getCapacity() : 40)
                .scheduleJson(request.getScheduleJson())
                .build();

        section = sectionRepository.save(section);
        log.info("Section created: {} - {}", course.getCode(), section.getSectionNumber());

        String instructorName = getInstructorName(instructor);
        return CourseSectionResponse.from(section, instructorName);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseSectionResponse getSectionById(Long id) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", id));
        String instructorName = getInstructorName(section.getInstructor());
        return CourseSectionResponse.from(section, instructorName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSectionResponse> getAllSections() {
        return sectionRepository.findAll().stream()
                .map(section -> CourseSectionResponse.from(section, getInstructorName(section.getInstructor())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSectionResponse> getSectionsByCourse(Long courseId) {
        return sectionRepository.findByCourseId(courseId).stream()
                .map(section -> CourseSectionResponse.from(section, getInstructorName(section.getInstructor())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSectionResponse> getSectionsBySemesterAndYear(String semester, Integer year) {
        return sectionRepository.findBySemesterAndYear(semester, year).stream()
                .map(section -> CourseSectionResponse.from(section, getInstructorName(section.getInstructor())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseSectionResponse> getSectionsBySemesterAndYear(String semester, Integer year, Pageable pageable) {
        Page<CourseSectionResponse> page = sectionRepository.findBySemesterAndYear(semester, year, pageable)
                .map(section -> CourseSectionResponse.from(section, getInstructorName(section.getInstructor())));
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSectionResponse> getInstructorSections(Long userId, String semester, Integer year) {
        return sectionRepository.findByInstructorUserIdAndSemesterAndYear(userId, semester, year).stream()
                .map(section -> CourseSectionResponse.from(section, getInstructorName(section.getInstructor())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseSectionResponse updateSection(Long id, UpdateSectionRequest request) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", id));

        if (request.getInstructorId() != null) {
            Faculty instructor = facultyRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor", request.getInstructorId()));
            section.setInstructor(instructor);
        }
        if (request.getCapacity() != null) {
            section.setCapacity(request.getCapacity());
        }
        if (request.getScheduleJson() != null) {
            section.setScheduleJson(request.getScheduleJson());
        }

        section = sectionRepository.save(section);
        log.info("Section updated: {}", id);

        return CourseSectionResponse.from(section, getInstructorName(section.getInstructor()));
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", id));
        sectionRepository.delete(section);
        log.info("Section deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        return sectionRepository.findDistinctYears();
    }

    private String getInstructorName(Faculty instructor) {
        if (instructor == null || instructor.getUserId() == null) {
            return "Unknown Instructor";
        }
        return userRepository.findById(instructor.getUserId())
                .map(User::getFullName)
                .orElse("Unknown Instructor");
    }
}