package com.smartcampus.academic.service.impl;

import com.smartcampus.academic.dto.request.CreateCourseRequest;
import com.smartcampus.academic.dto.request.UpdateCourseRequest;
import com.smartcampus.academic.dto.response.CourseResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.entity.Course;
import com.smartcampus.academic.entity.Department;
import com.smartcampus.academic.exception.ConflictException;
import com.smartcampus.academic.exception.ResourceNotFoundException;
import com.smartcampus.academic.repository.CourseRepository;
import com.smartcampus.academic.repository.DepartmentRepository;
import com.smartcampus.academic.service.CourseService;
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
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Bu ders kodu zaten kullanılıyor: " + request.getCode());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Departman", request.getDepartmentId()));

        Course course = Course.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .credits(request.getCredits())
                .ects(request.getEcts())
                .department(department)
                .syllabusUrl(request.getSyllabusUrl())
                .build();

        course = courseRepository.save(course);
        log.info("Ders oluşturuldu: {}", course.getCode());

        return CourseResponse.from(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ders", id));
        return CourseResponse.from(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseByCode(String code) {
        Course course = courseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Ders bulunamadı: " + code));
        return CourseResponse.from(course);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getAllCourses(Pageable pageable) {
        Page<CourseResponse> page = courseRepository.findAll(pageable)
                .map(CourseResponse::from);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCoursesByDepartment(Long departmentId, Pageable pageable) {
        Page<CourseResponse> page = courseRepository.findByDepartmentId(departmentId, pageable)
                .map(CourseResponse::from);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> searchCourses(String keyword, Long departmentId, Pageable pageable) {
        Page<CourseResponse> page;
        if (departmentId != null) {
            page = courseRepository.searchByDepartmentAndKeyword(departmentId, keyword, pageable)
                    .map(CourseResponse::from);
        } else {
            page = courseRepository.searchByKeyword(keyword, pageable)
                    .map(CourseResponse::from);
        }
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ders", id));

        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getCredits() != null) {
            course.setCredits(request.getCredits());
        }
        if (request.getEcts() != null) {
            course.setEcts(request.getEcts());
        }
        if (request.getSyllabusUrl() != null) {
            course.setSyllabusUrl(request.getSyllabusUrl());
        }

        course = courseRepository.save(course);
        log.info("Ders güncellendi: {}", course.getCode());

        return CourseResponse.from(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ders", id));
        courseRepository.delete(course);
        log.info("Ders silindi: {}", course.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByDepartmentList(Long departmentId) {
        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(CourseResponse::from)
                .collect(Collectors.toList());
    }
}
