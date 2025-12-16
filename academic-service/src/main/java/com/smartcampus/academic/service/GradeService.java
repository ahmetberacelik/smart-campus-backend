package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.request.UpdateGradeRequest;
import com.smartcampus.academic.dto.response.EnrollmentResponse;
import com.smartcampus.academic.dto.response.TranscriptResponse;

import java.util.List;

/**
 * Not yönetimi servisi.
 * Öğrenci notlarını görüntüleme, transkript oluşturma ve not girişi işlemlerini yönetir.
 */
public interface GradeService {
    
    /**
     * Öğrencinin tüm notlarını getir
     * @param studentUserId Öğrencinin user ID'si
     * @return Enrollment listesi (notlar dahil)
     */
    List<EnrollmentResponse> getStudentGrades(Long studentUserId);
    
    /**
     * Öğrencinin transkriptini getir
     * @param studentUserId Öğrencinin user ID'si
     * @return Transkript bilgileri
     */
    TranscriptResponse getTranscript(Long studentUserId);
    
    /**
     * Tek bir enrollment için not gir
     * @param facultyUserId Öğretim görevlisinin user ID'si
     * @param request Not bilgileri
     * @return Güncellenen enrollment
     */
    EnrollmentResponse enterGrade(Long facultyUserId, UpdateGradeRequest request);
    
    /**
     * Toplu not girişi
     * @param facultyUserId Öğretim görevlisinin user ID'si
     * @param requests Not bilgileri listesi
     * @return Güncellenen enrollment'lar
     */
    List<EnrollmentResponse> enterGradesBatch(Long facultyUserId, List<UpdateGradeRequest> requests);
}
