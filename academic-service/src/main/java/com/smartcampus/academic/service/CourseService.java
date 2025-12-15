package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.request.CreateCourseRequest;
import com.smartcampus.academic.dto.request.UpdateCourseRequest;
import com.smartcampus.academic.dto.response.CourseResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CreateCourseRequest request);

    CourseResponse getCourseById(Long id);

    CourseResponse getCourseByCode(String code);

    PageResponse<CourseResponse> getAllCourses(Pageable pageable);

    PageResponse<CourseResponse> getCoursesByDepartment(Long departmentId, Pageable pageable);

    PageResponse<CourseResponse> searchCourses(String keyword, Long departmentId, Pageable pageable);

    CourseResponse updateCourse(Long id, UpdateCourseRequest request);

    void deleteCourse(Long id);

    List<CourseResponse> getCoursesByDepartmentList(Long departmentId);
}
