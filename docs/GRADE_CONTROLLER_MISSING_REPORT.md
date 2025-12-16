# 🔴 GradeController Eksik - Backend Düzeltme Raporu

## Sorun Özeti

**Tarih:** 16 Aralık 2025  
**Öncelik:** YÜKSEK  
**Etkilenen Servis:** Academic-Service  
**Belirti:** Frontend `/api/v1/grades/my-grades` endpoint'ine istek yapıyor ama backend 500 Internal Server Error döndürüyor.

---

## Teknik Analiz

### Mevcut Durum

1. **API Gateway** `/grades/**` route'unu academic-service'e yönlendiriyor ✅
   ```properties
   # application.properties - satır 51-53
   spring.cloud.gateway.routes[7].id=grades-service
   spring.cloud.gateway.routes[7].uri=http://${ACADEMIC_SERVICE_HOST:localhost}:${ACADEMIC_SERVICE_PORT:8082}
   spring.cloud.gateway.routes[7].predicates[0]=Path=/api/v1/grades/**
   ```

2. **Enrollment Entity** not alanlarını içeriyor ✅
   - `midtermGrade`, `finalGrade`, `homeworkGrade`
   - `letterGrade`, `gradePoint`
   - `calculateTotalGrade()` metodu

3. **Repository'ler** hazır ✅
   - `StudentRepository.findByUserId()` ✅
   - `FacultyRepository.findByUserId()` ✅
   - `EnrollmentRepository.findByStudentId()` ✅
   - `EnrollmentRepository.findByStudentIdAndStatus()` ✅

4. **DTO'lar** hazır ✅
   - `EnrollmentResponse` ✅
   - `TranscriptResponse` ✅
   - `UpdateGradeRequest` ✅

5. **GradeController YOK! ❌**
   Academic-service'de mevcut controller'lar:
   - `ClassroomController.java`
   - `CourseController.java`
   - `EnrollmentController.java`
   - `SectionController.java`

---

## Frontend'in Beklediği Endpoint'ler

| Endpoint | HTTP Method | Açıklama | Rol |
|----------|-------------|----------|-----|
| `/api/v1/grades/my-grades` | GET | Öğrencinin notları | STUDENT |
| `/api/v1/grades/transcript` | GET | Transkript (JSON) | STUDENT |
| `/api/v1/grades/transcript/pdf` | GET | Transkript (PDF) | STUDENT |
| `/api/v1/grades` | POST | Not girişi | FACULTY |

---

## Çözüm: 3 Dosya Oluşturulması Gerekiyor

### Dosya 1: GradeController.java

**Konum:** `academic-service/src/main/java/com/smartcampus/academic/controller/GradeController.java`

```java
package com.smartcampus.academic.controller;

import com.smartcampus.academic.dto.request.UpdateGradeRequest;
import com.smartcampus.academic.dto.response.ApiResponse;
import com.smartcampus.academic.dto.response.EnrollmentResponse;
import com.smartcampus.academic.dto.response.TranscriptResponse;
import com.smartcampus.academic.security.CurrentUser;
import com.smartcampus.academic.service.GradeService;
import com.smartcampus.academic.service.TranscriptPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final TranscriptPdfService transcriptPdfService;

    /**
     * Öğrencinin notları
     * GET /api/v1/grades/my-grades
     */
    @GetMapping("/my-grades")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyGrades(@CurrentUser Long userId) {
        List<EnrollmentResponse> grades = gradeService.getStudentGrades(userId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    /**
     * Transkript (JSON)
     * GET /api/v1/grades/transcript
     */
    @GetMapping("/transcript")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<TranscriptResponse>> getTranscript(@CurrentUser Long userId) {
        TranscriptResponse transcript = gradeService.getTranscript(userId);
        return ResponseEntity.ok(ApiResponse.success(transcript));
    }

    /**
     * Transkript (PDF)
     * GET /api/v1/grades/transcript/pdf
     */
    @GetMapping("/transcript/pdf")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<byte[]> getTranscriptPdf(@CurrentUser Long userId) {
        byte[] pdfBytes = transcriptPdfService.generateTranscriptPdf(userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "transcript.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Not girişi (Faculty)
     * POST /api/v1/grades
     */
    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enterGrade(
            @CurrentUser Long facultyUserId,
            @RequestBody UpdateGradeRequest request) {
        EnrollmentResponse enrollment = gradeService.enterGrade(facultyUserId, request);
        return ResponseEntity.ok(ApiResponse.success(enrollment, "Not başarıyla girildi"));
    }

    /**
     * Toplu not girişi (Faculty)
     * POST /api/v1/grades/batch
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> enterGradesBatch(
            @CurrentUser Long facultyUserId,
            @RequestBody List<UpdateGradeRequest> requests) {
        List<EnrollmentResponse> results = gradeService.enterGradesBatch(facultyUserId, requests);
        return ResponseEntity.ok(ApiResponse.success(results, "Notlar başarıyla girildi"));
    }
}
```

---

### Dosya 2: GradeService.java (Interface)

**Konum:** `academic-service/src/main/java/com/smartcampus/academic/service/GradeService.java`

```java
package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.request.UpdateGradeRequest;
import com.smartcampus.academic.dto.response.EnrollmentResponse;
import com.smartcampus.academic.dto.response.TranscriptResponse;

import java.util.List;

public interface GradeService {
    
    /**
     * Öğrencinin tüm notlarını getir
     */
    List<EnrollmentResponse> getStudentGrades(Long studentUserId);
    
    /**
     * Öğrencinin transkriptini getir
     */
    TranscriptResponse getTranscript(Long studentUserId);
    
    /**
     * Tek bir enrollment için not gir
     */
    EnrollmentResponse enterGrade(Long facultyUserId, UpdateGradeRequest request);
    
    /**
     * Toplu not girişi
     */
    List<EnrollmentResponse> enterGradesBatch(Long facultyUserId, List<UpdateGradeRequest> requests);
}
```

---

### Dosya 3: GradeServiceImpl.java

**Konum:** `academic-service/src/main/java/com/smartcampus/academic/service/impl/GradeServiceImpl.java`

```java
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Override
    public List<EnrollmentResponse> getStudentGrades(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        
        return enrollments.stream()
                .map(e -> EnrollmentResponse.from(e, 
                        student.getUser().getFirstName() + " " + student.getUser().getLastName()))
                .collect(Collectors.toList());
    }

    @Override
    public TranscriptResponse getTranscript(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        List<Enrollment> allEnrollments = enrollmentRepository.findByStudentId(student.getId());
        List<Enrollment> completedEnrollments = allEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .collect(Collectors.toList());

        // Dönem bazlı grupla
        Map<String, List<Enrollment>> bySemester = completedEnrollments.stream()
                .collect(Collectors.groupingBy(e -> 
                        e.getSection().getSemester().name() + "-" + e.getSection().getYear()));

        List<TranscriptResponse.SemesterRecord> semesterRecords = new ArrayList<>();
        BigDecimal totalPoints = BigDecimal.ZERO;
        int totalCredits = 0;

        for (Map.Entry<String, List<Enrollment>> entry : bySemester.entrySet()) {
            List<Enrollment> semEnrollments = entry.getValue();
            
            BigDecimal semesterPoints = BigDecimal.ZERO;
            int semesterCredits = 0;
            List<TranscriptResponse.CourseRecord> courseRecords = new ArrayList<>();

            for (Enrollment e : semEnrollments) {
                int credits = e.getSection().getCourse().getCredits();
                BigDecimal gradePoint = e.getGradePoint() != null ? e.getGradePoint() : BigDecimal.ZERO;
                
                semesterPoints = semesterPoints.add(gradePoint.multiply(BigDecimal.valueOf(credits)));
                semesterCredits += credits;

                courseRecords.add(TranscriptResponse.CourseRecord.builder()
                        .courseCode(e.getSection().getCourse().getCode())
                        .courseName(e.getSection().getCourse().getName())
                        .credits(credits)
                        .letterGrade(e.getLetterGrade())
                        .gradePoint(gradePoint)
                        .build());
            }

            BigDecimal semesterGpa = semesterCredits > 0 
                    ? semesterPoints.divide(BigDecimal.valueOf(semesterCredits), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String[] parts = entry.getKey().split("-");
            semesterRecords.add(TranscriptResponse.SemesterRecord.builder()
                    .semester(parts[0])
                    .year(Integer.parseInt(parts[1]))
                    .gpa(semesterGpa)
                    .credits(semesterCredits)
                    .courses(courseRecords)
                    .build());

            totalPoints = totalPoints.add(semesterPoints);
            totalCredits += semesterCredits;
        }

        BigDecimal cgpa = totalCredits > 0 
                ? totalPoints.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return TranscriptResponse.builder()
                .studentId(student.getId())
                .studentNumber(student.getStudentNumber())
                .studentName(student.getUser().getFirstName() + " " + student.getUser().getLastName())
                .departmentName(student.getDepartment().getName())
                .cgpa(cgpa)
                .totalCredits(totalCredits)
                .completedCredits(totalCredits)
                .semesters(semesterRecords)
                .build();
    }

    @Override
    @Transactional
    public EnrollmentResponse enterGrade(Long facultyUserId, UpdateGradeRequest request) {
        Faculty faculty = facultyRepository.findByUserId(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretim görevlisi bulunamadı"));

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt bulunamadı"));

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
            
            if (request.getFinalGrade() != null) {
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
            }
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        Student student = saved.getStudent();
        return EnrollmentResponse.from(saved, 
                student.getUser().getFirstName() + " " + student.getUser().getLastName());
    }

    @Override
    @Transactional
    public List<EnrollmentResponse> enterGradesBatch(Long facultyUserId, List<UpdateGradeRequest> requests) {
        return requests.stream()
                .map(request -> enterGrade(facultyUserId, request))
                .collect(Collectors.toList());
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
        if (grade >= 55) return "FD";
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
```

---

## Özet

**Oluşturulacak Dosyalar (3 adet):**

| Dosya | Konum |
|-------|-------|
| `GradeController.java` | `controller/` |
| `GradeService.java` | `service/` |
| `GradeServiceImpl.java` | `service/impl/` |

**Mevcut ve Hazır (değişiklik gerekmez):**
- `StudentRepository.java` ✅
- `FacultyRepository.java` ✅
- `EnrollmentRepository.java` ✅
- `TranscriptResponse.java` ✅
- `EnrollmentResponse.java` ✅
- `UpdateGradeRequest.java` ✅

---

## Test Senaryoları

| Endpoint | Test | Beklenen |
|----------|------|----------|
| `GET /api/v1/grades/my-grades` | STUDENT token | 200 + notlar listesi |
| `GET /api/v1/grades/transcript` | STUDENT token | 200 + transkript JSON |
| `GET /api/v1/grades/transcript/pdf` | STUDENT token | 200 + PDF |
| `POST /api/v1/grades` | FACULTY token | 200 + güncellenen enrollment |
| `GET /api/v1/grades/my-grades` | FACULTY token | 403 Forbidden |


