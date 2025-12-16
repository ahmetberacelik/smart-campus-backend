package com.smartcampus.attendance.service.impl;

import com.smartcampus.attendance.dto.request.ExcuseRequestDto;
import com.smartcampus.attendance.dto.request.ReviewExcuseRequest;
import com.smartcampus.attendance.dto.response.ExcuseRequestResponse;
import com.smartcampus.attendance.dto.response.PageResponse;
import com.smartcampus.attendance.entity.*;
import com.smartcampus.attendance.exception.BadRequestException;
import com.smartcampus.attendance.exception.ForbiddenException;
import com.smartcampus.attendance.exception.ResourceNotFoundException;
import com.smartcampus.attendance.repository.AttendanceRecordRepository;
import com.smartcampus.attendance.repository.AttendanceSessionRepository;
import com.smartcampus.attendance.repository.ExcuseRequestRepository;
import com.smartcampus.attendance.service.ExcuseRequestService;
import com.smartcampus.attendance.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcuseRequestServiceImpl implements ExcuseRequestService {

    private final ExcuseRequestRepository excuseRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ExcuseRequestResponse createExcuseRequest(Long studentId, ExcuseRequestDto request, MultipartFile document) {
        AttendanceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Yoklama oturumu", "id", request.getSessionId()));

        // Zaten mazeret başvurusu var mı kontrol et
        if (excuseRequestRepository.existsBySessionIdAndStudentId(request.getSessionId(), studentId)) {
            throw new BadRequestException("Bu yoklama için zaten mazeret başvurusu yapılmış", "EXCUSE_ALREADY_EXISTS");
        }

        String documentUrl = null;
        if (document != null && !document.isEmpty()) {
            documentUrl = fileStorageService.uploadFile(document, "excuse-documents");
        }

        ExcuseRequest excuseRequest = ExcuseRequest.builder()
                .sessionId(request.getSessionId())
                .studentId(studentId)
                .reason(request.getReason())
                .documentUrl(documentUrl)
                .status(ExcuseStatus.PENDING)
                .build();

        excuseRequest = excuseRequestRepository.save(excuseRequest);

        return mapToResponse(excuseRequest, session);
    }

    @Override
    public PageResponse<ExcuseRequestResponse> getExcuseRequestsForFaculty(Long instructorId, Long sectionId,
                                                                            ExcuseStatus status, Pageable pageable) {
        try {
            Page<ExcuseRequest> requests = excuseRequestRepository.findByInstructorIdWithFilters(
                    instructorId, sectionId, status, pageable);

            List<ExcuseRequestResponse> content = requests.getContent().stream()
                    .map(excuseRequest -> {
                        try {
                            // Session'ı bul
                            AttendanceSession session = sessionRepository
                                    .findById(excuseRequest.getSessionId())
                                    .orElse(null);
                            
                            if (session != null) {
                                return mapToResponse(excuseRequest, session);
                            }
                            
                            // Session bulunamazsa basit response döndür
                            return mapToResponse(excuseRequest);
                        } catch (Exception e) {
                            log.error("Mazeret response oluşturulurken hata: excuseId={}, error={}", 
                                    excuseRequest.getId(), e.getMessage());
                            return mapToResponse(excuseRequest);
                        }
                    })
                    .toList();

            return PageResponse.from(requests, content);
        } catch (Exception e) {
            log.error("Mazeret istekleri getirilirken hata: instructorId={}, error={}", instructorId, e.getMessage(), e);
            throw new RuntimeException("Mazeret istekleri yüklenirken bir hata oluştu", e);
        }
    }

    @Override
    public PageResponse<ExcuseRequestResponse> getMyExcuseRequests(Long studentId, Pageable pageable) {
        Page<ExcuseRequest> requests = excuseRequestRepository.findByStudentId(studentId, pageable);

        List<ExcuseRequestResponse> content = requests.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.from(requests, content);
    }

    @Override
    @Transactional
    public ExcuseRequestResponse approveExcuseRequest(Long instructorId, Long requestId, ReviewExcuseRequest request) {
        ExcuseRequest excuseRequest = excuseRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Mazeret başvurusu", "id", requestId));

        validateCanReview(excuseRequest, instructorId);

        excuseRequest.setStatus(ExcuseStatus.APPROVED);
        excuseRequest.setReviewedBy(instructorId);
        excuseRequest.setReviewedAt(LocalDateTime.now());
        excuseRequest.setReviewerNotes(request.getNotes());

        ExcuseRequest savedExcuseRequest = excuseRequestRepository.save(excuseRequest);

        // İlgili attendance record'u EXCUSED olarak işaretle
        attendanceRecordRepository.findBySessionIdAndStudentId(excuseRequest.getSessionId(), excuseRequest.getStudentId())
                .ifPresent(record -> {
                    record.setStatus(AttendanceStatus.EXCUSED);
                    attendanceRecordRepository.save(record);
                });

        return mapToResponse(savedExcuseRequest);
    }

    @Override
    @Transactional
    public ExcuseRequestResponse rejectExcuseRequest(Long instructorId, Long requestId, ReviewExcuseRequest request) {
        ExcuseRequest excuseRequest = excuseRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Mazeret başvurusu", "id", requestId));

        validateCanReview(excuseRequest, instructorId);

        excuseRequest.setStatus(ExcuseStatus.REJECTED);
        excuseRequest.setReviewedBy(instructorId);
        excuseRequest.setReviewedAt(LocalDateTime.now());
        excuseRequest.setReviewerNotes(request.getNotes());

        excuseRequest = excuseRequestRepository.save(excuseRequest);

        return mapToResponse(excuseRequest);
    }

    private void validateCanReview(ExcuseRequest excuseRequest, Long instructorId) {
        if (excuseRequest.getStatus() != ExcuseStatus.PENDING) {
            throw new BadRequestException("Mazeret zaten değerlendirilmiş", "EXCUSE_ALREADY_REVIEWED");
        }

        AttendanceSession session = sessionRepository.findById(excuseRequest.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Yoklama oturumu", "id", excuseRequest.getSessionId()));

        if (!session.getInstructorId().equals(instructorId)) {
            throw new ForbiddenException("Bu mazereti değerlendirme yetkiniz yok");
        }
    }

    private ExcuseRequestResponse mapToResponse(ExcuseRequest excuseRequest) {
        return ExcuseRequestResponse.builder()
                .id(excuseRequest.getId())
                .studentId(excuseRequest.getStudentId())
                .sessionId(excuseRequest.getSessionId())
                .reason(excuseRequest.getReason())
                .documentUrl(excuseRequest.getDocumentUrl())
                .status(excuseRequest.getStatus())
                .createdAt(excuseRequest.getCreatedAt())
                .reviewedAt(excuseRequest.getReviewedAt())
                .reviewerNotes(excuseRequest.getReviewerNotes())
                .build();
    }

    private ExcuseRequestResponse mapToResponse(ExcuseRequest excuseRequest, AttendanceSession session) {
        return ExcuseRequestResponse.builder()
                .id(excuseRequest.getId())
                .studentId(excuseRequest.getStudentId())
                .sessionId(session.getId())
                .date(session.getSessionDate())
                .reason(excuseRequest.getReason())
                .documentUrl(excuseRequest.getDocumentUrl())
                .status(excuseRequest.getStatus())
                .createdAt(excuseRequest.getCreatedAt())
                .reviewedAt(excuseRequest.getReviewedAt())
                .reviewerNotes(excuseRequest.getReviewerNotes())
                .build();
    }
}
