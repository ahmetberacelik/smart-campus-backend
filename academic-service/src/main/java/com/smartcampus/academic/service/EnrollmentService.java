package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.request.EnrollRequest;
import com.smartcampus.academic.dto.request.UpdateGradeRequest;
import com.smartcampus.academic.dto.response.EnrollmentResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.dto.response.TranscriptResponse;
import com.smartcampus.academic.entity.Semester;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enrollStudent(Long userId, EnrollRequest request);

    EnrollmentResponse getEnrollmentById(Long id);

    List<EnrollmentResponse> getStudentEnrollments(Long userId);

    List<EnrollmentResponse> getStudentEnrollmentsBySemester(Long userId, Semester semester, Integer year);

    PageResponse<EnrollmentResponse> getSectionEnrollments(Long sectionId, Pageable pageable);

    EnrollmentResponse updateGrade(Long enrollmentId, UpdateGradeRequest request);

    void dropEnrollment(Long userId, Long sectionId);

    TranscriptResponse getStudentTranscript(Long userId);

    List<EnrollmentResponse> getActiveEnrollments(Long userId);

    boolean isStudentEnrolled(Long userId, Long sectionId);
}
