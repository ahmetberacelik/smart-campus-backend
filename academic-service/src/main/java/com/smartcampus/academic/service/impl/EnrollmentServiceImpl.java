package com.smartcampus.academic.service.impl;

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

    @Override
    @Transactional
    public EnrollmentResponse enrollStudent(Long userId, EnrollRequest request) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        CourseSection section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", request.getSectionId()));

        if (enrollmentRepository.existsByStudentIdAndSectionId(student.getId(), section.getId())) {
            throw new ConflictException("Bu derse zaten kayıtlısınız");
        }

        if (!section.hasAvailableSlots()) {
            throw new BadRequestException("Bu section'da yer kalmadı");
        }

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
        Page<EnrollmentResponse> page = enrollmentRepository.findBySectionId(sectionId, pageable)
                .map(e -> EnrollmentResponse.from(e, getStudentName(e.getStudent())));
        return PageResponse.from(page);
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
        log.info("Not güncellendi: enrollment={}", enrollmentId);

        return EnrollmentResponse.from(enrollment, getStudentName(enrollment.getStudent()));
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