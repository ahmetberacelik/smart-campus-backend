package com.smartcampus.academic.service.impl;

import com.smartcampus.academic.dto.request.CreateSectionRequest;
import com.smartcampus.academic.dto.request.UpdateSectionRequest;
import com.smartcampus.academic.dto.response.CourseSectionResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.entity.Classroom;
import com.smartcampus.academic.entity.Course;
import com.smartcampus.academic.entity.CourseSection;
import com.smartcampus.academic.entity.Faculty;
import com.smartcampus.academic.entity.Role;
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
import java.util.Optional;
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

        // instructorId User ID olarak gelebilir, önce Faculty ID olarak kontrol et
        // Eğer bulunamazsa User ID olarak kabul et ve Faculty'yi bul
        log.info("🔍 createSection: Looking for instructor with ID: {}", request.getInstructorId());
        Faculty instructor = facultyRepository.findById(request.getInstructorId())
                .orElse(null);
        
        if (instructor == null) {
            // User ID olarak kabul et ve Faculty'yi bul
            log.info("🔍 createSection: instructorId {} not found as Faculty ID, trying as User ID", request.getInstructorId());
            
            // Önce User'ın var olup olmadığını kontrol et
            User user = userRepository.findById(request.getInstructorId()).orElse(null);
            if (user == null) {
                log.error("❌ createSection: User not found with ID: {}", request.getInstructorId());
                throw new ResourceNotFoundException("User", request.getInstructorId());
            }
            
            log.info("✅ createSection: User found - ID: {}, Email: {}, Role: {}", user.getId(), user.getEmail(), user.getRole());
            
            // User FACULTY rolünde mi kontrol et
            if (user.getRole() == null || user.getRole() != Role.FACULTY) {
                log.error("❌ createSection: User {} is not a FACULTY member. Role: {}", request.getInstructorId(), user.getRole());
                throw new ResourceNotFoundException("Instructor", request.getInstructorId());
            }
            
            // Faculty'yi bul - önce userId field'ı ile, sonra user.id ile
            Optional<Faculty> facultyOpt = Optional.empty();
            
            // Query ile bulmayı dene
            try {
                facultyOpt = facultyRepository.findByUserId(request.getInstructorId());
                if (facultyOpt.isPresent()) {
                    log.info("✅ createSection: Found Faculty via findByUserId query");
                } else {
                    log.warn("⚠️ createSection: findByUserId query returned empty");
                }
            } catch (Exception e) {
                log.warn("⚠️ createSection: findByUserId query failed: {}", e.getMessage());
            }
            
            // Eğer bulunamazsa, tüm Faculty'leri kontrol et
            if (facultyOpt.isEmpty()) {
                log.warn("⚠️ createSection: Checking all faculty records manually...");
                try {
                    List<Faculty> allFaculty = facultyRepository.findAll();
                    log.info("📋 createSection: Total faculty records in database: {}", allFaculty.size());
                    for (Faculty f : allFaculty) {
                        log.info("  - Faculty ID: {}, User ID: {}, User: {}", 
                            f.getId(), 
                            f.getUserId(), 
                            f.getUser() != null ? f.getUser().getId() : "null");
                    }
                    
                    // Manuel olarak user.id ile kontrol et
                    for (Faculty f : allFaculty) {
                        try {
                            // Önce userId field'ını kontrol et
                            if (f.getUserId() != null && f.getUserId().equals(request.getInstructorId())) {
                                facultyOpt = Optional.of(f);
                                log.info("✅ createSection: Found Faculty ID {} via userId field", f.getId());
                                break;
                            }
                            // Sonra user relationship'ini kontrol et (lazy loading için)
                            try {
                                if (f.getUser() != null && f.getUser().getId() != null && f.getUser().getId().equals(request.getInstructorId())) {
                                    facultyOpt = Optional.of(f);
                                    log.info("✅ createSection: Found Faculty ID {} via user relationship", f.getId());
                                    break;
                                }
                            } catch (Exception e) {
                                // Lazy loading hatası olabilir, devam et
                                log.debug("⚠️ createSection: Could not access user for Faculty ID {}: {}", f.getId(), e.getMessage());
                            }
                        } catch (Exception e) {
                            log.warn("⚠️ createSection: Error checking Faculty ID {}: {}", f.getId(), e.getMessage());
                            continue;
                        }
                    }
                } catch (Exception e) {
                    log.error("❌ createSection: Error fetching all faculty: {}", e.getMessage(), e);
                }
            }
            
            if (facultyOpt.isEmpty()) {
                log.error("❌ createSection: Faculty record not found for User ID: {}. User exists but Faculty record is missing. This user may need to be registered as a faculty member first.", request.getInstructorId());
                throw new ResourceNotFoundException("Instructor", request.getInstructorId());
            }
            instructor = facultyOpt.get();
            log.info("✅ createSection: Found Faculty ID {} for User ID {}", instructor.getId(), request.getInstructorId());
        } else {
            log.info("✅ createSection: Found Faculty ID {} directly", instructor.getId());
        }

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
    public List<CourseSectionResponse> getSectionsByInstructor(Long instructorId) {
        return sectionRepository.findByInstructorId(instructorId).stream()
                .map(section -> CourseSectionResponse.from(section, getInstructorName(section.getInstructor())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSectionResponse> getSectionsByInstructorUserId(Long userId) {
        // Tüm exception'ları yakala ve boş liste döndür
        try {
            log.info("🔍 getSectionsByInstructorUserId: Looking for Faculty with userId: {}", userId);
            
            if (userId == null) {
                log.warn("⚠️ getSectionsByInstructorUserId: userId is null. Returning empty list.");
                return List.of();
            }
            
            // User ID'den Faculty ID'yi bul - önce query ile, sonra manuel kontrol
            Optional<Faculty> facultyOpt = Optional.empty();
            
            // Query ile bulmayı dene
            try {
                facultyOpt = facultyRepository.findByUserId(userId);
                if (facultyOpt.isPresent()) {
                    log.info("✅ getSectionsByInstructorUserId: Found Faculty via findByUserId query");
                } else {
                    log.warn("⚠️ getSectionsByInstructorUserId: findByUserId query returned empty");
                }
            } catch (Exception e) {
                log.warn("⚠️ getSectionsByInstructorUserId: findByUserId query failed: {}", e.getMessage());
            }
            
            // Eğer bulunamazsa, tüm Faculty'leri kontrol et
            if (facultyOpt.isEmpty()) {
                log.warn("⚠️ getSectionsByInstructorUserId: Checking all faculty records manually...");
                try {
                    List<Faculty> allFaculty = facultyRepository.findAll();
                    log.info("📋 getSectionsByInstructorUserId: Total faculty records in database: {}", allFaculty.size());
                    
                    // Manuel olarak user.id ile kontrol et
                    for (Faculty f : allFaculty) {
                        try {
                            // Önce userId field'ını kontrol et
                            if (f.getUserId() != null && f.getUserId().equals(userId)) {
                                facultyOpt = Optional.of(f);
                                log.info("✅ getSectionsByInstructorUserId: Found Faculty ID {} via userId field", f.getId());
                                break;
                            }
                            // Sonra user relationship'ini kontrol et (lazy loading için)
                            try {
                                if (f.getUser() != null && f.getUser().getId() != null && f.getUser().getId().equals(userId)) {
                                    facultyOpt = Optional.of(f);
                                    log.info("✅ getSectionsByInstructorUserId: Found Faculty ID {} via user relationship", f.getId());
                                    break;
                                }
                            } catch (Exception e) {
                                // Lazy loading hatası olabilir, devam et
                                log.debug("⚠️ getSectionsByInstructorUserId: Could not access user for Faculty ID {}: {}", f.getId(), e.getMessage());
                            }
                        } catch (Exception e) {
                            log.warn("⚠️ getSectionsByInstructorUserId: Error checking Faculty ID {}: {}", f.getId(), e.getMessage());
                            continue;
                        }
                    }
                } catch (Exception e) {
                    log.error("❌ getSectionsByInstructorUserId: Error fetching all faculty: {}", e.getMessage(), e);
                }
            }
            
            if (facultyOpt.isEmpty()) {
                log.warn("⚠️ getSectionsByInstructorUserId: Faculty not found for userId: {}. Returning empty list.", userId);
                return List.of(); // Faculty bulunamazsa boş liste döndür
            }
            
            Faculty faculty = facultyOpt.get();
            log.info("✅ getSectionsByInstructorUserId: Found Faculty ID: {} for userId: {}", faculty.getId(), userId);
            
            // Sections'ları getir
            List<CourseSection> sections;
            try {
                sections = sectionRepository.findByInstructorId(faculty.getId());
                log.info("✅ getSectionsByInstructorUserId: Found {} sections for Faculty ID: {}", sections.size(), faculty.getId());
            } catch (Exception e) {
                log.error("❌ getSectionsByInstructorUserId: Error fetching sections for Faculty ID {}: {}", faculty.getId(), e.getMessage(), e);
                return List.of();
            }
            
            // Her section için güvenli bir şekilde response oluştur
            List<CourseSectionResponse> responses = new java.util.ArrayList<>();
            for (CourseSection section : sections) {
                try {
                    String instructorName = getInstructorName(section.getInstructor());
                    CourseSectionResponse response = CourseSectionResponse.from(section, instructorName);
                    responses.add(response);
                } catch (Exception e) {
                    log.error("❌ getSectionsByInstructorUserId: Error processing section ID {}: {}", section.getId(), e.getMessage(), e);
                    // Hata olsa bile devam et, sadece bu section'ı atla
                }
            }
            
            log.info("✅ getSectionsByInstructorUserId: Successfully processed {} sections", responses.size());
            return responses;
            
        } catch (Exception e) {
            log.error("❌ getSectionsByInstructorUserId: Unexpected error for userId {}: {}", userId, e.getMessage(), e);
            log.error("❌ Stack trace: ", e);
            // Hata durumunda boş liste döndür, 500 hatası verme
            return List.of();
        }
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
        try {
            if (instructor == null) {
                return "Unknown Instructor";
            }
            
            // Önce userId field'ını kontrol et
            if (instructor.getUserId() != null) {
                return userRepository.findById(instructor.getUserId())
                        .map(User::getFullName)
                        .orElse("Unknown Instructor");
            }
            
            // Eğer userId null ise, user relationship'ini kontrol et
            if (instructor.getUser() != null) {
                return instructor.getUser().getFullName();
            }
            
            return "Unknown Instructor";
        } catch (Exception e) {
            log.warn("⚠️ getInstructorName: Error getting instructor name: {}", e.getMessage());
            return "Unknown Instructor";
        }
    }
}