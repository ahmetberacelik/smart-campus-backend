package com.smartcampus.academic.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.academic.dto.ScheduleSlot;
import com.smartcampus.academic.dto.request.EnrollRequest;
import com.smartcampus.academic.dto.request.UpdateGradeRequest;
import com.smartcampus.academic.dto.response.EnrollmentResponse;
import com.smartcampus.academic.dto.response.PageResponse;
import com.smartcampus.academic.dto.response.TranscriptResponse;
import com.smartcampus.academic.entity.*;
import com.smartcampus.academic.exception.BadRequestException;
import com.smartcampus.academic.exception.ConflictException;
import com.smartcampus.academic.exception.ResourceNotFoundException;
import com.smartcampus.academic.repository.*;
import com.smartcampus.academic.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseSectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public EnrollmentResponse enrollStudent(Long userId, EnrollRequest request) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        CourseSection section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", request.getSectionId()));

        // Öğrencinin sadece kendi bölümündeki derslere kayıt olmasına izin ver
        Department studentDept = student.getDepartment();
        Department courseDept = section.getCourse().getDepartment();
        if (studentDept != null && courseDept != null
                && !studentDept.getId().equals(courseDept.getId())) {
            throw new BadRequestException("Bu ders kendi bölümünüz dışında. Bu derse kayıt olamazsınız.");
        }

        if (enrollmentRepository.existsByStudentIdAndSectionId(student.getId(), section.getId())) {
            throw new ConflictException("Bu derse zaten kayıtlısınız");
        }

        if (!section.hasAvailableSlots()) {
            throw new BadRequestException("Bu section'da yer kalmadı");
        }

        checkPrerequisites(student.getId(), section.getCourse().getId());

        checkScheduleConflict(student.getId(), section);

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .section(section)
                .status(EnrollmentStatus.ENROLLED)
                .build();

        section.incrementEnrollment();
        sectionRepository.save(section);

        enrollment = enrollmentRepository.save(enrollment);
        log.info("Öğrenci derse kayıt oldu: {} -> {}", student.getStudentNumber(), section.getCourse().getCode());

        String studentName = getStudentName(student);
        return EnrollmentResponse.from(enrollment, studentName);
    }

    private void checkPrerequisites(Long studentId, Long courseId) {
        Set<Long> prerequisiteIds = prerequisiteRepository.findPrerequisiteIdsByCourseId(courseId);
        if (prerequisiteIds.isEmpty()) {
            return;
        }

        Set<Long> completedCourseIds = enrollmentRepository.findCompletedCourseIdsByStudentId(studentId);
        
        Set<Long> allRequiredPrerequisites = new HashSet<>();
        collectAllPrerequisites(courseId, allRequiredPrerequisites, new HashSet<>());
        
        List<String> missingPrerequisites = new ArrayList<>();
        for (Long prereqId : allRequiredPrerequisites) {
            if (!completedCourseIds.contains(prereqId)) {
                List<String> codes = prerequisiteRepository.findPrerequisiteCodesByCourseId(courseId);
                missingPrerequisites.addAll(codes.stream()
                        .filter(code -> !completedCourseIds.contains(prereqId))
                        .toList());
            }
        }

        if (!missingPrerequisites.isEmpty()) {
            throw new BadRequestException("Önkoşul dersleri tamamlanmamış: " + String.join(", ", missingPrerequisites));
        }
    }

    private void collectAllPrerequisites(Long courseId, Set<Long> allPrereqs, Set<Long> visited) {
        if (visited.contains(courseId)) {
            return;
        }
        visited.add(courseId);

        Set<Long> directPrereqs = prerequisiteRepository.findPrerequisiteIdsByCourseId(courseId);
        for (Long prereqId : directPrereqs) {
            allPrereqs.add(prereqId);
            collectAllPrerequisites(prereqId, allPrereqs, visited);
        }
    }

    private void checkScheduleConflict(Long studentId, CourseSection newSection) {
        if (newSection.getScheduleJson() == null || newSection.getScheduleJson().isEmpty()) {
            return;
        }

        List<CourseSection> enrolledSections = enrollmentRepository.findActiveEnrolledSectionsByStudentId(
                studentId, newSection.getSemester(), newSection.getYear());

        List<ScheduleSlot> newSlots = parseScheduleJson(newSection.getScheduleJson());
        if (newSlots.isEmpty()) {
            return;
        }

        for (CourseSection existingSection : enrolledSections) {
            if (existingSection.getScheduleJson() == null) {
                continue;
            }
            List<ScheduleSlot> existingSlots = parseScheduleJson(existingSection.getScheduleJson());
            
            for (ScheduleSlot newSlot : newSlots) {
                for (ScheduleSlot existingSlot : existingSlots) {
                    if (newSlot.overlapsWith(existingSlot)) {
                        throw new ConflictException(String.format(
                                "Ders çakışması: %s ile %s dersleri %s günü çakışıyor",
                                newSection.getCourse().getCode(),
                                existingSection.getCourse().getCode(),
                                newSlot.getDay()));
                    }
                }
            }
        }
    }

    private List<ScheduleSlot> parseScheduleJson(String scheduleJson) {
        try {
            return objectMapper.readValue(scheduleJson, new TypeReference<List<ScheduleSlot>>() {});
        } catch (Exception e) {
            log.warn("Schedule JSON parse hatası: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt", id));
        String studentName = getStudentName(enrollment.getStudent());
        return EnrollmentResponse.from(enrollment, studentName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        return enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(e -> EnrollmentResponse.from(e, getStudentName(e.getStudent())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollmentsBySemester(Long userId, Semester semester, Integer year) {
        return enrollmentRepository.findByStudentUserIdAndSemesterAndYear(userId, semester, year).stream()
                .map(e -> EnrollmentResponse.from(e, getStudentName(e.getStudent())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getSectionEnrollments(Long sectionId, Pageable pageable) {
        try {
            log.info("getSectionEnrollments çağrıldı - sectionId: {}", sectionId);
            
            Page<Enrollment> enrollmentPage = enrollmentRepository.findBySectionId(sectionId, pageable);
            log.info("Bulunan enrollment sayısı: {} (toplam: {})", enrollmentPage.getNumberOfElements(), enrollmentPage.getTotalElements());
            
            // Her enrollment'ı ayrı ayrı işle, hata olanları atlayarak devam et
            List<EnrollmentResponse> responses = new ArrayList<>();
            for (Enrollment enrollment : enrollmentPage.getContent()) {
                try {
                    String studentName = getStudentName(enrollment.getStudent());
                    EnrollmentResponse response = EnrollmentResponse.from(enrollment, studentName);
                    responses.add(response);
                    log.debug("Enrollment işlendi - enrollmentId: {}, studentId: {}, studentName: {}", 
                            enrollment.getId(), enrollment.getStudent() != null ? enrollment.getStudent().getId() : "null", studentName);
                } catch (Exception ex) {
                    log.error("EnrollmentResponse.from hatası - enrollmentId: {}, sectionId: {}, error: {}", 
                            enrollment.getId(), sectionId, ex.getMessage(), ex);
                    // Hata olan enrollment'ı atla, diğerlerini işlemeye devam et
                }
            }
            
            log.info("Başarıyla işlenen enrollment sayısı: {}", responses.size());
            
            // PageResponse oluştur (manuel olarak)
            Page<EnrollmentResponse> page = new org.springframework.data.domain.PageImpl<>(
                    responses, 
                    pageable, 
                    enrollmentPage.getTotalElements()
            );
            
            log.info("getSectionEnrollments tamamlandı - sectionId: {}, dönen enrollment sayısı: {}, toplam: {}", 
                    sectionId, responses.size(), page.getTotalElements());
            
            return PageResponse.from(page);
        } catch (Exception e) {
            log.error("getSectionEnrollments hatası - sectionId: {}, error: {}", sectionId, e.getMessage(), e);
            throw new RuntimeException("Section öğrencileri getirilirken hata oluştu: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public EnrollmentResponse updateGrade(Long enrollmentId, UpdateGradeRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt", enrollmentId));

        if (request.getMidtermGrade() != null) {
            enrollment.setMidtermGrade(request.getMidtermGrade());
        }
        if (request.getFinalGrade() != null) {
            enrollment.setFinalGrade(request.getFinalGrade());
        }
        if (request.getHomeworkGrade() != null) {
            enrollment.setHomeworkGrade(request.getHomeworkGrade());
        }

        BigDecimal totalGrade = enrollment.calculateTotalGrade();
        if (totalGrade != null) {
            String letterGrade = calculateLetterGrade(totalGrade);
            BigDecimal gradePoint = calculateGradePoint(letterGrade);
            enrollment.setLetterGrade(letterGrade);
            enrollment.setGradePoint(gradePoint);
        }

        enrollment = enrollmentRepository.save(enrollment);
        
        updateStudentGpa(enrollment.getStudent().getId());
        
        log.info("Not güncellendi: enrollment={}", enrollmentId);

        return EnrollmentResponse.from(enrollment, getStudentName(enrollment.getStudent()));
    }

    private void updateStudentGpa(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci", studentId));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        int totalCredits = 0;
        BigDecimal totalPoints = BigDecimal.ZERO;

        for (Enrollment e : enrollments) {
            if (e.getGradePoint() != null && e.getStatus() == EnrollmentStatus.COMPLETED) {
                int credits = e.getSection().getCourse().getCredits();
                totalCredits += credits;
                totalPoints = totalPoints.add(e.getGradePoint().multiply(new BigDecimal(credits)));
            }
        }

        BigDecimal cgpa = totalCredits > 0 
                ? totalPoints.divide(new BigDecimal(totalCredits), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        student.setCgpa(cgpa);
        student.setGpa(cgpa);
        studentRepository.save(student);
        log.info("Öğrenci GPA güncellendi: studentId={}, cgpa={}", studentId, cgpa);
    }

    @Override
    @Transactional
    public void dropEnrollment(Long userId, Long sectionId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndSectionId(student.getId(), sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt bulunamadı"));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new BadRequestException("Bu kayıt zaten bırakılmış veya tamamlanmış");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment.getSection().decrementEnrollment();

        sectionRepository.save(enrollment.getSection());
        enrollmentRepository.save(enrollment);

        log.info("Ders bırakıldı: {} -> {}", student.getStudentNumber(), enrollment.getSection().getCourse().getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public TranscriptResponse getStudentTranscript(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", userId));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());

        Map<String, List<Enrollment>> semesterGroups = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED || e.getLetterGrade() != null)
                .collect(Collectors.groupingBy(e -> 
                        e.getSection().getSemester() + "-" + e.getSection().getYear()));

        List<TranscriptResponse.SemesterRecord> semesters = new ArrayList<>();
        int totalCredits = 0;
        int completedCredits = 0;
        BigDecimal totalPoints = BigDecimal.ZERO;

        for (Map.Entry<String, List<Enrollment>> entry : semesterGroups.entrySet()) {
            String[] parts = entry.getKey().split("-");
            String semesterName = parts[0];
            int year = Integer.parseInt(parts[1]);

            List<TranscriptResponse.CourseRecord> courses = new ArrayList<>();
            int semesterCredits = 0;
            BigDecimal semesterPoints = BigDecimal.ZERO;

            for (Enrollment e : entry.getValue()) {
                int credits = e.getSection().getCourse().getCredits();
                courses.add(TranscriptResponse.CourseRecord.builder()
                        .courseCode(e.getSection().getCourse().getCode())
                        .courseName(e.getSection().getCourse().getName())
                        .credits(credits)
                        .letterGrade(e.getLetterGrade())
                        .gradePoint(e.getGradePoint())
                        .build());

                if (e.getGradePoint() != null) {
                    semesterCredits += credits;
                    semesterPoints = semesterPoints.add(e.getGradePoint().multiply(new BigDecimal(credits)));
                    totalCredits += credits;
                    totalPoints = totalPoints.add(e.getGradePoint().multiply(new BigDecimal(credits)));
                    if (e.getGradePoint().compareTo(BigDecimal.ZERO) > 0) {
                        completedCredits += credits;
                    }
                }
            }

            BigDecimal gpa = semesterCredits > 0 ? 
                    semesterPoints.divide(new BigDecimal(semesterCredits), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            semesters.add(TranscriptResponse.SemesterRecord.builder()
                    .semester(semesterName)
                    .year(year)
                    .gpa(gpa)
                    .credits(semesterCredits)
                    .courses(courses)
                    .build());
        }

        BigDecimal cgpa = totalCredits > 0 ? 
                totalPoints.divide(new BigDecimal(totalCredits), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return TranscriptResponse.builder()
                .studentId(student.getId())
                .studentNumber(student.getStudentNumber())
                .studentName(user.getFullName())
                .departmentName(student.getDepartment().getName())
                .cgpa(cgpa)
                .totalCredits(totalCredits)
                .completedCredits(completedCredits)
                .semesters(semesters)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getActiveEnrollments(Long userId) {
        return enrollmentRepository.findActiveEnrollmentsByUserId(userId).stream()
                .map(e -> EnrollmentResponse.from(e, getStudentName(e.getStudent())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolled(Long userId, Long sectionId) {
        Student student = studentRepository.findByUserId(userId).orElse(null);
        if (student == null) return false;
        return enrollmentRepository.existsByStudentIdAndSectionId(student.getId(), sectionId);
    }

    private String getStudentName(Student student) {
        return userRepository.findById(student.getUserId())
                .map(User::getFullName)
                .orElse("Bilinmeyen Öğrenci");
    }

    private String calculateLetterGrade(BigDecimal totalGrade) {
        double grade = totalGrade.doubleValue();
        if (grade >= 90) return "AA";
        if (grade >= 85) return "BA";
        if (grade >= 80) return "BB";
        if (grade >= 75) return "CB";
        if (grade >= 70) return "CC";
        if (grade >= 65) return "DC";
        if (grade >= 60) return "DD";
        if (grade >= 50) return "FD";
        return "FF";
    }

    private BigDecimal calculateGradePoint(String letterGrade) {
        return switch (letterGrade) {
            case "AA" -> new BigDecimal("4.00");
            case "BA" -> new BigDecimal("3.50");
            case "BB" -> new BigDecimal("3.00");
            case "CB" -> new BigDecimal("2.50");
            case "CC" -> new BigDecimal("2.00");
            case "DC" -> new BigDecimal("1.50");
            case "DD" -> new BigDecimal("1.00");
            case "FD" -> new BigDecimal("0.50");
            default -> BigDecimal.ZERO;
        };
    }
}