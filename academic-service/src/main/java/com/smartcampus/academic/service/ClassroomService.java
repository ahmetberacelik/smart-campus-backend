package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.request.CreateClassroomRequest;
import com.smartcampus.academic.dto.request.UpdateClassroomRequest;
import com.smartcampus.academic.dto.response.ClassroomResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClassroomService {

    ClassroomResponse createClassroom(CreateClassroomRequest request);

    ClassroomResponse getClassroomById(Long id);

    PageResponse<ClassroomResponse> getAllClassrooms(Pageable pageable);

    PageResponse<ClassroomResponse> getActiveClassrooms(Pageable pageable);

    PageResponse<ClassroomResponse> getClassroomsByBuilding(String building, Pageable pageable);

    PageResponse<ClassroomResponse> searchClassrooms(String keyword, Pageable pageable);

    ClassroomResponse updateClassroom(Long id, UpdateClassroomRequest request);

    void deleteClassroom(Long id);

    List<String> getBuildings();

    List<ClassroomResponse> getClassroomsByMinCapacity(Integer minCapacity);
}
