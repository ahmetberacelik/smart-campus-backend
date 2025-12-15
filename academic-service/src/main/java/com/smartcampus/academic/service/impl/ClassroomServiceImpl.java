package com.smartcampus.academic.service.impl;

import com.smartcampus.academic.dto.request.CreateClassroomRequest;
import com.smartcampus.academic.dto.request.UpdateClassroomRequest;
import com.smartcampus.academic.dto.response.ClassroomResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.entity.Classroom;
import com.smartcampus.academic.exception.ConflictException;
import com.smartcampus.academic.exception.ResourceNotFoundException;
import com.smartcampus.academic.repository.ClassroomRepository;
import com.smartcampus.academic.service.ClassroomService;
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
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;

    @Override
    @Transactional
    public ClassroomResponse createClassroom(CreateClassroomRequest request) {
        if (classroomRepository.findByBuildingAndRoomNumber(request.getBuilding(), request.getRoomNumber()).isPresent()) {
            throw new ConflictException("Bu derslik zaten mevcut: " + request.getBuilding() + " - " + request.getRoomNumber());
        }

        Classroom classroom = Classroom.builder()
                .building(request.getBuilding())
                .roomNumber(request.getRoomNumber())
                .capacity(request.getCapacity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .featuresJson(request.getFeaturesJson())
                .build();

        classroom = classroomRepository.save(classroom);
        log.info("Derslik oluşturuldu: {}", classroom.getFullName());

        return ClassroomResponse.from(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomResponse getClassroomById(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Derslik", id));
        return ClassroomResponse.from(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassroomResponse> getAllClassrooms(Pageable pageable) {
        Page<ClassroomResponse> page = classroomRepository.findAll(pageable)
                .map(ClassroomResponse::from);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassroomResponse> getActiveClassrooms(Pageable pageable) {
        Page<ClassroomResponse> page = classroomRepository.findByIsActiveTrue(pageable)
                .map(ClassroomResponse::from);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassroomResponse> getClassroomsByBuilding(String building, Pageable pageable) {
        Page<ClassroomResponse> page = classroomRepository.findByBuilding(building, pageable)
                .map(ClassroomResponse::from);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassroomResponse> searchClassrooms(String keyword, Pageable pageable) {
        Page<ClassroomResponse> page = classroomRepository.searchByKeyword(keyword, pageable)
                .map(ClassroomResponse::from);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public ClassroomResponse updateClassroom(Long id, UpdateClassroomRequest request) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Derslik", id));

        if (request.getCapacity() != null) {
            classroom.setCapacity(request.getCapacity());
        }
        if (request.getLatitude() != null) {
            classroom.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            classroom.setLongitude(request.getLongitude());
        }
        if (request.getFeaturesJson() != null) {
            classroom.setFeaturesJson(request.getFeaturesJson());
        }
        if (request.getIsActive() != null) {
            classroom.setIsActive(request.getIsActive());
        }

        classroom = classroomRepository.save(classroom);
        log.info("Derslik güncellendi: {}", classroom.getFullName());

        return ClassroomResponse.from(classroom);
    }

    @Override
    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Derslik", id));
        classroomRepository.delete(classroom);
        log.info("Derslik silindi: {}", classroom.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getBuildings() {
        return classroomRepository.findDistinctBuildings();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomResponse> getClassroomsByMinCapacity(Integer minCapacity) {
        return classroomRepository.findByMinCapacity(minCapacity).stream()
                .map(ClassroomResponse::from)
                .collect(Collectors.toList());
    }
}
