package com.smartcampus.attendance.service.impl;

import com.smartcampus.attendance.dto.request.CheckInQrRequest;
import com.smartcampus.attendance.dto.request.CheckInRequest;
import com.smartcampus.attendance.dto.request.CreateSessionRequest;
import com.smartcampus.attendance.dto.response.*;
import com.smartcampus.attendance.entity.*;
import com.smartcampus.attendance.exception.BadRequestException;
import com.smartcampus.attendance.exception.ForbiddenException;
import com.smartcampus.attendance.exception.ResourceNotFoundException;
import com.smartcampus.attendance.repository.AttendanceRecordRepository;
import com.smartcampus.attendance.repository.AttendanceSessionRepository;
import com.smartcampus.attendance.service.AttendanceService;
import com.smartcampus.attendance.util.GpsUtils;
import com.smartcampus.attendance.util.QrCodeGenerator;
import com.smartcampus.attendance.util.SpoofingDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final GpsUtils gpsUtils;
    private final QrCodeGenerator qrCodeGenerator;
    private final SpoofingDetector spoofingDetector;

    @Value("${attendance.default-geofence-radius:15}")
    private int defaultGeofenceRadius;

    @Value("${attendance.qr-code-refresh-interval:5}")
    private int qrCodeRefreshInterval;

    @Override
    @Transactional
    public SessionResponse createSession(Long instructorId, CreateSessionRequest request) {
        AttendanceSession session = AttendanceSession.builder()
                .sectionId(request.getSectionId())
                .instructorId(instructorId)
                .sessionDate(LocalDate.now())
                .startTime(LocalTime.now())
                .endTime(request.getDurationMinutes() != null
                        ? LocalTime.now().plusMinutes(request.getDurationMinutes())
                        : null)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .geofenceRadius(request.getGeofenceRadius() != null
                        ? request.getGeofenceRadius() : defaultGeofenceRadius)
                .status(SessionStatus.ACTIVE)
                .build();

        String qrCode = qrCodeGenerator.generateQrCode(session.getId());
        session.setQrCode(qrCode);
        session.setQrCodeGeneratedAt(LocalDateTime.now());

        session = sessionRepository.save(session);

        String finalQrCode = qrCodeGenerator.generateQrCode(session.getId());
        session.setQrCode(finalQrCode);
        session.setQrCodeGeneratedAt(LocalDateTime.now());
        session = sessionRepository.save(session);

        return mapToSessionResponse(session);
    }

    @Override
    public SessionResponse getSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Yoklama oturumu", "id", sessionId));
        return mapToSessionResponse(session);
    }

    @Override
    @Transactional
    public SessionResponse closeSession(Long instructorId, Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Yoklama oturumu", "id", sessionId));

        if (!session.getInstructorId().equals(instructorId)) {
            throw new ForbiddenException("Bu oturumu kapatma yetkiniz yok");
        }

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Oturum zaten kapatılmış", "SESSION_NOT_ACTIVE");
        }

        session.setStatus(SessionStatus.CLOSED);
        session.setEndTime(LocalTime.now());
        session = sessionRepository.save(session);

        return mapToSessionResponse(session);
    }

    @Override
    public PageResponse<SessionResponse> getMySessions(Long instructorId, Long sectionId, SessionStatus status,
                                                        LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<AttendanceSession> sessions = sessionRepository.findByInstructorIdWithFilters(
                instructorId, sectionId, status, startDate, endDate, pageable);

        List<SessionResponse> content = sessions.getContent().stream()
                .map(this::mapToSessionResponse)
                .toList();

        return PageResponse.from(sessions, content);
    }

    @Override
    public AttendanceReportResponse getAttendanceReport(Long instructorId, Long sectionId,
                                                         LocalDate startDate, LocalDate endDate) {
        List<AttendanceSession> sessions = sessionRepository.findBySectionIdAndDateRange(sectionId, startDate, endDate);

        if (sessions.isEmpty()) {
            return AttendanceReportResponse.builder()
                    .sectionId(sectionId)
                    .totalSessions(0)
                    .students(Collections.emptyList())
                    .build();
        }

        if (!sessions.get(0).getInstructorId().equals(instructorId)) {
            throw new ForbiddenException("Bu dersin raporunu görüntüleme yetkiniz yok");
        }

        List<Long> sessionIds = sessions.stream().map(AttendanceSession::getId).toList();
        List<AttendanceRecord> allRecords = recordRepository.findBySessionIds(sessionIds);

        Map<Long, List<AttendanceRecord>> recordsByStudent = new HashMap<>();
        for (AttendanceRecord record : allRecords) {
            recordsByStudent.computeIfAbsent(record.getStudentId(), k -> new ArrayList<>()).add(record);
        }

        List<AttendanceReportResponse.StudentAttendance> studentAttendances = new ArrayList<>();
        for (Map.Entry<Long, List<AttendanceRecord>> entry : recordsByStudent.entrySet()) {
            Long studentId = entry.getKey();
            List<AttendanceRecord> records = entry.getValue();

            int presentCount = (int) records.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            int excusedCount = (int) records.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.EXCUSED).count();
            int absentCount = sessions.size() - presentCount - excusedCount;

            double percentage = sessions.isEmpty() ? 0 :
                    ((double) (presentCount + excusedCount) / sessions.size()) * 100;

            boolean isFlagged = records.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsFlagged()));
            String flagReason = records.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsFlagged()))
                    .map(AttendanceRecord::getFlagReason)
                    .findFirst()
                    .orElse(null);

            studentAttendances.add(AttendanceReportResponse.StudentAttendance.builder()
                    .studentId(studentId)
                    .presentCount(presentCount)
                    .absentCount(absentCount)
                    .excusedCount(excusedCount)
                    .attendancePercentage(Math.round(percentage * 10) / 10.0)
                    .status(getAttendanceStatusString(percentage))
                    .isFlagged(isFlagged)
                    .flagReason(flagReason)
                    .build());
        }

        return AttendanceReportResponse.builder()
                .sectionId(sectionId)
                .totalSessions(sessions.size())
                .students(studentAttendances)
                .build();
    }

    @Override
    @Transactional
    public CheckInResponse checkIn(Long studentId, Long sessionId, CheckInRequest request, String ipAddress) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Yoklama oturumu", "id", sessionId));

        validateSession(session);
        checkAlreadyCheckedIn(sessionId, studentId);

        double distance = gpsUtils.calculateDistance(
                session.getLatitude(), session.getLongitude(),
                request.getLatitude(), request.getLongitude());

        if (distance > session.getGeofenceRadius()) {
            throw new BadRequestException("Derslik konumunun dışındasınız",
                    "OUT_OF_GEOFENCE",
                    Map.of("distance", Math.round(distance * 10) / 10.0,
                            "allowedRadius", session.getGeofenceRadius(),
                            "message", String.format("Derslikten %.1f metre uzaktasınız. Maksimum mesafe: %d metre",
                                    distance, session.getGeofenceRadius())));
        }

        Optional<AttendanceRecord> lastRecord = recordRepository.findLastRecordByStudentId(studentId);
        SpoofingDetector.SpoofingResult spoofingResult = spoofingDetector.detectSpoofing(
                request.getLatitude(), request.getLongitude(), request.getAccuracy(),
                request.getIsMockLocation(), lastRecord);

        if (spoofingResult.isFlagged()) {
            throw new ForbiddenException("Şüpheli konum verisi tespit edildi",
                    "GPS_SPOOFING_DETECTED",
                    Map.of("reason", spoofingResult.reason(), "message", spoofingResult.message()));
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .sessionId(sessionId)
                .studentId(studentId)
                .status(AttendanceStatus.PRESENT)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(CheckInMethod.GPS)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .distanceFromClassroom(distance)
                .gpsAccuracy(request.getAccuracy())
                .ipAddress(ipAddress)
                .deviceInfo(request.getDeviceInfo())
                .isFlagged(false)
                .build();

        recordRepository.save(record);

        return CheckInResponse.builder()
                .sessionId(sessionId)
                .checkInTime(record.getCheckInTime())
                .distance(Math.round(distance * 10) / 10.0)
                .method(CheckInMethod.GPS)
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    @Override
    @Transactional
    public CheckInResponse checkInWithQr(Long studentId, Long sessionId, CheckInQrRequest request, String ipAddress) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Yoklama oturumu", "id", sessionId));

        validateSession(session);
        checkAlreadyCheckedIn(sessionId, studentId);

        try {
            QrCodeGenerator.QrCodeData qrData = qrCodeGenerator.parseQrCode(request.getQrCode());
            if (!qrData.sessionId().equals(sessionId)) {
                throw new BadRequestException("Geçersiz QR kod", "INVALID_QR_CODE");
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Geçersiz QR kod formatı", "INVALID_QR_CODE");
        }

        if (qrCodeGenerator.isQrCodeExpired(request.getQrCode(), qrCodeRefreshInterval)) {
            throw new BadRequestException("QR kod süresi dolmuş", "QR_CODE_EXPIRED");
        }

        double distance = gpsUtils.calculateDistance(
                session.getLatitude(), session.getLongitude(),
                request.getLatitude(), request.getLongitude());

        if (distance > session.getGeofenceRadius()) {
            throw new BadRequestException("Derslik konumunun dışındasınız", "OUT_OF_GEOFENCE",
                    Map.of("distance", Math.round(distance * 10) / 10.0,
                            "allowedRadius", session.getGeofenceRadius()));
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .sessionId(sessionId)
                .studentId(studentId)
                .status(AttendanceStatus.PRESENT)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(CheckInMethod.QR_CODE)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .distanceFromClassroom(distance)
                .gpsAccuracy(request.getAccuracy())
                .ipAddress(ipAddress)
                .deviceInfo(request.getDeviceInfo())
                .isFlagged(false)
                .build();

        recordRepository.save(record);

        return CheckInResponse.builder()
                .sessionId(sessionId)
                .checkInTime(record.getCheckInTime())
                .distance(Math.round(distance * 10) / 10.0)
                .method(CheckInMethod.QR_CODE)
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    @Override
    public List<MyAttendanceResponse> getMyAttendance(Long studentId, String semester, Integer year) {
        List<AttendanceRecord> records = recordRepository.findByStudentId(studentId);

        Map<Long, List<AttendanceRecord>> recordsBySession = new HashMap<>();
        for (AttendanceRecord record : records) {
            recordsBySession.computeIfAbsent(record.getSessionId(), k -> new ArrayList<>()).add(record);
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional
    public SessionResponse refreshQrCode(Long instructorId, Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Yoklama oturumu", "id", sessionId));

        if (!session.getInstructorId().equals(instructorId)) {
            throw new ForbiddenException("Bu oturumun QR kodunu yenileme yetkiniz yok");
        }

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Oturum aktif değil", "SESSION_NOT_ACTIVE");
        }

        String newQrCode = qrCodeGenerator.generateQrCode(sessionId);
        session.setQrCode(newQrCode);
        session.setQrCodeGeneratedAt(LocalDateTime.now());
        session = sessionRepository.save(session);

        return mapToSessionResponse(session);
    }

    private void validateSession(AttendanceSession session) {
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Yoklama oturumu aktif değil", "SESSION_NOT_ACTIVE");
        }
    }

    private void checkAlreadyCheckedIn(Long sessionId, Long studentId) {
        if (recordRepository.findBySessionIdAndStudentId(sessionId, studentId).isPresent()) {
            throw new BadRequestException("Bu oturuma zaten yoklama verdiniz", "ALREADY_CHECKED_IN");
        }
    }

    private SessionResponse mapToSessionResponse(AttendanceSession session) {
        Long presentCount = recordRepository.countBySessionIdAndStatus(session.getId(), AttendanceStatus.PRESENT);

        return SessionResponse.builder()
                .id(session.getId())
                .sectionId(session.getSectionId())
                .date(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .latitude(session.getLatitude())
                .longitude(session.getLongitude())
                .geofenceRadius(session.getGeofenceRadius())
                .qrCode(session.getQrCode())
                .qrCodeUrl(session.getQrCode() != null ? qrCodeGenerator.getQrCodeUrl(session.getQrCode()) : null)
                .status(session.getStatus())
                .presentCount(presentCount.intValue())
                .build();
    }

    private String getAttendanceStatusString(double percentage) {
        if (percentage >= 80) return "OK";
        if (percentage >= 70) return "WARNING";
        return "CRITICAL";
    }
}
