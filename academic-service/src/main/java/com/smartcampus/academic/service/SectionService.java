package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.request.CreateSectionRequest;
import com.smartcampus.academic.dto.request.UpdateSectionRequest;
import com.smartcampus.academic.dto.response.CourseSectionResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SectionService {

    CourseSectionResponse createSection(CreateSectionRequest request);

    CourseSectionResponse getSectionById(Long id);

    List<CourseSectionResponse> getAllSections();

    List<CourseSectionResponse> getSectionsByCourse(Long courseId);

    List<CourseSectionResponse> getSectionsBySemesterAndYear(String semester, Integer year);

    PageResponse<CourseSectionResponse> getSectionsBySemesterAndYear(String semester, Integer year, Pageable pageable);

    List<CourseSectionResponse> getInstructorSections(Long userId, String semester, Integer year);

    CourseSectionResponse updateSection(Long id, UpdateSectionRequest request);

    void deleteSection(Long id);

    List<Integer> getAvailableYears();
}