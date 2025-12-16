package com.smartcampus.academic.service.impl;

import com.smartcampus.academic.dto.request.UpdateGradeRequest;
import com.smartcampus.academic.dto.response.EnrollmentResponse;
import com.smartcampus.academic.dto.response.TranscriptResponse;
import com.smartcampus.academic.entity.*;
import com.smartcampus.academic.exception.ForbiddenException;
import com.smartcampus.academic.exception.ResourceNotFoundException;
import com.smartcampus.academic.repository.*;
import com.smartcampus.academic.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentGrades(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        String studentName = getStudentName(student);
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        
        return enrollments.stream()
                .map(e -> EnrollmentResponse.from(e, studentName))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TranscriptResponse getTranscript(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());

        // Sadece tamamlanan veya notu girilen dersleri al
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
                .studentName(student.getUser().getFullName())
                .departmentName(student.getDepartment().getName())
                .cgpa(cgpa)
                .totalCredits(totalCredits)
                .completedCredits(completedCredits)
                .semesters(semesters)
                .build();
    }

    @Override
    @Transactional
    public EnrollmentResponse enterGrade(Long facultyUserId, UpdateGradeRequest request) {
        Faculty faculty = facultyRepository.findByUserId(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretim görevlisi bulunamadı"));

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt", request.getEnrollmentId()));

        // Yetki kontrolü: Section'ın instructor'ı mı?
        if (!enrollment.getSection().getInstructor().getId().equals(faculty.getId())) {
            throw new ForbiddenException("Bu ders için not girme yetkiniz yok");
        }

        // Notları güncelle
        if (request.getMidtermGrade() != null) {
            enrollment.setMidtermGrade(request.getMidtermGrade());
        }
        if (request.getFinalGrade() != null) {
            enrollment.setFinalGrade(request.getFinalGrade());
        }
        if (request.getHomeworkGrade() != null) {
            enrollment.setHomeworkGrade(request.getHomeworkGrade());
        }
        
        // Harf notu hesapla
        BigDecimal totalGrade = enrollment.calculateTotalGrade();
        if (totalGrade != null) {
            String letterGrade = calculateLetterGrade(totalGrade);
            enrollment.setLetterGrade(letterGrade);
            enrollment.setGradePoint(getGradePoint(letterGrade));
            
            // Final notu girildiyse dersi tamamlanmış olarak işaretle
            if (request.getFinalGrade() != null) {
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
            }
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Not girildi: enrollment={}, letterGrade={}", saved.getId(), saved.getLetterGrade());
        
        // Öğrenci GPA güncelle
        updateStudentGpa(saved.getStudent().getId());
        
        return EnrollmentResponse.from(saved, getStudentName(saved.getStudent()));
    }

    @Override
    @Transactional
    public List<EnrollmentResponse> enterGradesBatch(Long facultyUserId, List<UpdateGradeRequest> requests) {
        return requests.stream()
                .map(request -> enterGrade(facultyUserId, request))
                .collect(Collectors.toList());
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

    private String getStudentName(Student student) {
        if (student.getUser() != null) {
            return student.getUser().getFullName();
        }
        return "Bilinmeyen Öğrenci";
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

    private BigDecimal getGradePoint(String letterGrade) {
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

