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
import com.smartcampus.attendance.repository.CourseSectionInfoRepository;
import com.smartcampus.attendance.repository.ExcuseRequestRepository;
import com.smartcampus.attendance.service.AttendanceService;
import com.smartcampus.attendance.util.GpsUtils;
import com.smartcampus.attendance.util.IpValidator;
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
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final ExcuseRequestRepository excuseRequestRepository;
    private final CourseSectionInfoRepository courseSectionInfoRepository;
    private final JdbcTemplate jdbcTemplate;
    private final GpsUtils gpsUtils;
    private final QrCodeGenerator qrCodeGenerator;
    private final SpoofingDetector spoofingDetector;
    private final IpValidator ipValidator;

    @Value("${attendance.default-geofence-radius:15}")
    private int defaultGeofenceRadius;

    @Value("${attendance.qr-code-refresh-interval:5}")
    private int qrCodeRefreshInterval;

    // Varsayılan kampüs koordinatları (İstanbul)
    @Value("${attendance.default-latitude:41.0082}")
    private double defaultLatitude;

    @Value("${attendance.default-longitude:28.9784}")
    private double defaultLongitude;

    @Override
    @Transactional
    public SessionResponse createSession(Long instructorId, CreateSessionRequest request) {
        try {
            log.info("🔍 createSession: Creating attendance session for instructorId: {}, sectionId: {}", instructorId,
                    request.getSectionId());

            // GPS koordinatları null ise varsayılan değerleri kullan
            Double latitude = request.getLatitude() != null ? request.getLatitude() : defaultLatitude;
            Double longitude = request.getLongitude() != null ? request.getLongitude() : defaultLongitude;
            log.info("📍 createSession: Using coordinates - lat: {}, lng: {}", latitude, longitude);

            // Tarih ve saat - frontend'den gelirse kullan, yoksa şimdiki zamanı al
            LocalDate sessionDate = request.getSessionDate() != null ? request.getSessionDate() : LocalDate.now();
            LocalTime startTime = null;
            LocalTime endTime = null;

            try {
                if (request.getStartTime() != null) {
                    startTime = request.getStartTime().toLocalTime();
                } else {
                    startTime = LocalTime.now();
                }

                if (request.getEndTime() != null) {
                    endTime = request.getEndTime().toLocalTime();
                } else if (request.getDurationMinutes() != null) {
                    endTime = startTime.plusMinutes(request.getDurationMinutes());
                }
                log.info("⏰ createSession: Session time - date: {}, start: {}, end: {}", sessionDate, startTime,
                        endTime);
            } catch (Exception e) {
                log.error("❌ createSession: Error parsing time: {}", e.getMessage(), e);
                throw new BadRequestException("Tarih/saat formatı geçersiz: " + e.getMessage(), "INVALID_TIME_FORMAT");
            }

            // Section ID kontrolü
            if (request.getSectionId() == null) {
                log.error("❌ createSession: sectionId is null");
                throw new BadRequestException("Section ID zorunludur", "SECTION_ID_REQUIRED");
            }

            AttendanceSession session = AttendanceSession.builder()
                    .sectionId(request.getSectionId())
                    .instructorId(instructorId)
                    .sessionDate(sessionDate)
                    .startTime(startTime)
                    .endTime(endTime)
                    .latitude(latitude)
                    .longitude(longitude)
                    .geofenceRadius(request.getGeofenceRadius() != null
                            ? request.getGeofenceRadius()
                            : defaultGeofenceRadius)
                    .status(SessionStatus.ACTIVE)
                    .build();

            log.info("💾 createSession: Saving session to database...");
            // Önce session'ı kaydet ki ID oluşsun
            session = sessionRepository.save(session);
            log.info("✅ createSession: Session saved with ID: {}", session.getId());

            // Artık session.getId() != null, QR kodu oluşturabiliriz
            try {
                log.info("🔐 createSession: Generating QR code...");
                String qrCode = qrCodeGenerator.generateQrCode(session.getId());
                session.setQrCode(qrCode);
                session.setQrCodeGeneratedAt(LocalDateTime.now());
                session = sessionRepository.save(session);
                log.info("✅ createSession: QR code generated successfully");
            } catch (Exception e) {
                log.error("❌ createSession: Error generating QR code: {}", e.getMessage(), e);
                // QR kod hatası olsa bile session'ı döndür
            }

            SessionResponse response = mapToSessionResponse(session);
            log.info("✅ createSession: Session created successfully - ID: {}", session.getId());
            return response;

        } catch (BadRequestException | ResourceNotFoundException e) {
            log.error("❌ createSession: Business error: {}", e.getMessage());
            throw e; // Bu hataları tekrar fırlat
        } catch (Exception e) {
            log.error("❌ createSession: Unexpected error for instructorId {}: {}", instructorId, e.getMessage(), e);
            log.error("❌ Stack trace: ", e);
            throw new RuntimeException("Yoklama oturumu oluşturulurken beklenmeyen bir hata oluştu: " + e.getMessage(),
                    e);
        }
    }

    @Override
    public SessionResponse getSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Yoklama oturumu", "id", sessionId));
        return mapToSessionResponseWithCourseInfo(session);
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

        // Oturumu kapat
        session.setStatus(SessionStatus.CLOSED);
        session.setEndTime(LocalTime.now());
        session = sessionRepository.save(session);

        // Yoklama vermeyen öğrenciler için otomatik devamsızlık kaydı oluştur
        createAbsentRecordsForMissingStudents(session);

        return mapToSessionResponse(session);
    }

    /**
     * Session'a yoklama vermeyen öğrenciler için otomatik ABSENT kaydı oluşturur
     * Public yapıldı çünkü scheduler'dan da çağrılması gerekiyor
     */
    public void createAbsentRecordsForMissingStudents(AttendanceSession session) {
        try {
            log.info("Yoklama vermeyen öğrenciler için devamsızlık kaydı oluşturuluyor - sessionId: {}, sectionId: {}",
                    session.getId(), session.getSectionId());

            // Section'a kayıtlı öğrencileri bul (enrollments tablosundan, status =
            // ENROLLED)
            String sql = "SELECT DISTINCT e.student_id FROM enrollments e " +
                    "WHERE e.section_id = ? AND e.status = 'ENROLLED'";
            List<Long> enrolledStudentIds = jdbcTemplate.queryForList(sql, Long.class, session.getSectionId());

            if (enrolledStudentIds.isEmpty()) {
                log.info("Section'a kayıtlı öğrenci bulunamadı - sectionId: {}", session.getSectionId());
                return;
            }

            log.info("Section'a kayıtlı {} öğrenci bulundu", enrolledStudentIds.size());

            // Bu session'a yoklama vermiş öğrencileri bul
            List<AttendanceRecord> existingRecords = recordRepository.findBySessionId(session.getId());
            Set<Long> checkedInStudentIds = existingRecords.stream()
                    .map(AttendanceRecord::getStudentId)
                    .collect(Collectors.toSet());

            log.info("Bu session'a {} öğrenci yoklama vermiş", checkedInStudentIds.size());

            // Yoklama vermeyen öğrencileri bul
            List<Long> absentStudentIds = enrolledStudentIds.stream()
                    .filter(studentId -> !checkedInStudentIds.contains(studentId))
                    .collect(Collectors.toList());

            if (absentStudentIds.isEmpty()) {
                log.info("Tüm öğrenciler yoklama vermiş, devamsızlık kaydı oluşturulmayacak");
                return;
            }

            log.info("{} öğrenci için devamsızlık kaydı oluşturuluyor", absentStudentIds.size());

            // Yoklama vermeyen her öğrenci için ABSENT kaydı oluştur
            int createdCount = 0;
            for (Long studentId : absentStudentIds) {
                try {
                    // Zaten kayıt var mı kontrol et (çift kayıt önleme)
                    Optional<AttendanceRecord> existing = recordRepository.findBySessionIdAndStudentId(
                            session.getId(), studentId);
                    if (existing.isPresent()) {
                        log.debug("Öğrenci için zaten kayıt var, atlanıyor - studentId: {}", studentId);
                        continue;
                    }

                    AttendanceRecord absentRecord = AttendanceRecord.builder()
                            .sessionId(session.getId())
                            .studentId(studentId)
                            .status(AttendanceStatus.ABSENT)
                            .checkInTime(null) // Yoklama verilmediği için null
                            .checkInMethod(null)
                            .latitude(null)
                            .longitude(null)
                            .distanceFromClassroom(null)
                            .gpsAccuracy(null)
                            .isFlagged(false)
                            .flagReason(null)
                            .ipAddress(null)
                            .deviceInfo(null)
                            .build();

                    recordRepository.save(absentRecord);
                    createdCount++;
                    log.debug("Devamsızlık kaydı oluşturuldu - sessionId: {}, studentId: {}",
                            session.getId(), studentId);
                } catch (Exception e) {
                    log.error("Öğrenci için devamsızlık kaydı oluşturulurken hata - studentId: {}, error: {}",
                            studentId, e.getMessage(), e);
                    // Bir öğrenci için hata olsa bile diğerlerini işlemeye devam et
                }
            }

            log.info("{} öğrenci için devamsızlık kaydı başarıyla oluşturuldu - sessionId: {}",
                    createdCount, session.getId());

        } catch (Exception e) {
            log.error(
                    "Yoklama vermeyen öğrenciler için devamsızlık kaydı oluşturulurken hata - sessionId: {}, error: {}",
                    session.getId(), e.getMessage(), e);
            // Hata olsa bile session kapatma işlemini tamamla
        }
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

            double percentage = sessions.isEmpty() ? 0
                    : ((double) (presentCount + excusedCount) / sessions.size()) * 100;

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
        // validateCampusNetwork(ipAddress); // Kampüs ağı kontrolü devre dışı bırakıldı
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
        // validateCampusNetwork(ipAddress); // Kampüs ağı kontrolü devre dışı bırakıldı
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

        // Konum bilgisi varsa kontrol et, yoksa sadece QR kod doğrulaması yeterli
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();
        Double distance = null;

        if (latitude != null && longitude != null) {
            // Konum bilgisi varsa geofence kontrolü yap
            distance = gpsUtils.calculateDistance(
                    session.getLatitude(), session.getLongitude(),
                    latitude, longitude);

            if (distance > session.getGeofenceRadius()) {
                throw new BadRequestException("Derslik konumunun dışındasınız", "OUT_OF_GEOFENCE",
                        Map.of("distance", Math.round(distance * 10) / 10.0,
                                "allowedRadius", session.getGeofenceRadius()));
            }
        } else {
            // Konum bilgisi yoksa session'ın konumunu kullan (sadece kayıt için)
            latitude = session.getLatitude();
            longitude = session.getLongitude();
            distance = 0.0; // QR kod kullanıldığı için mesafe 0 kabul edilir
            log.info("📍 QR check-in: Location not provided, using session location");
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .sessionId(sessionId)
                .studentId(studentId)
                .status(AttendanceStatus.PRESENT)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(CheckInMethod.QR_CODE)
                .latitude(latitude)
                .longitude(longitude)
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
                .distance(distance != null ? Math.round(distance * 10) / 10.0 : 0.0)
                .method(CheckInMethod.QR_CODE)
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    @Override
    public List<MyAttendanceResponse> getMyAttendance(Long studentId, String semester, Integer year) {
        List<Long> sectionIds = sessionRepository.findDistinctSectionIdsByStudentId(studentId);
        if (sectionIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, CourseSectionInfo> sectionInfoMap = courseSectionInfoRepository.findBySectionIds(sectionIds);

        if (semester != null && year != null) {
            sectionIds = sectionIds.stream()
                    .filter(id -> {
                        CourseSectionInfo info = sectionInfoMap.get(id);
                        return info != null && semester.equals(info.getSemester()) && year.equals(info.getYear());
                    })
                    .toList();
        }

        List<MyAttendanceResponse> responses = new ArrayList<>();

        for (Long sectionId : sectionIds) {
            CourseSectionInfo sectionInfo = sectionInfoMap.get(sectionId);
            if (sectionInfo == null)
                continue;

            List<AttendanceSession> sessions = sessionRepository.findBySectionIdOrderByDateDesc(sectionId);
            if (sessions.isEmpty())
                continue;

            List<Long> sessionIds = sessions.stream().map(AttendanceSession::getId).toList();
            List<AttendanceRecord> records = recordRepository.findBySessionIdsAndStudentId(sessionIds, studentId);
            Map<Long, AttendanceRecord> recordMap = new HashMap<>();
            for (AttendanceRecord r : records) {
                recordMap.put(r.getSessionId(), r);
            }

            List<ExcuseRequest> excuses = excuseRequestRepository.findByStudentId(studentId);
            Map<Long, ExcuseRequest> excuseMap = new HashMap<>();
            for (ExcuseRequest e : excuses) {
                excuseMap.put(e.getSessionId(), e);
            }

            int presentCount = 0;
            int absentCount = 0;
            int excusedCount = 0;
            List<MyAttendanceResponse.SessionAttendance> sessionAttendances = new ArrayList<>();

            for (AttendanceSession session : sessions) {
                AttendanceRecord record = recordMap.get(session.getId());
                String status;
                LocalTime checkInTime = null;
                String excuseStatus = null;

                if (record != null) {
                    status = record.getStatus().name();
                    checkInTime = record.getCheckInTime() != null ? record.getCheckInTime().toLocalTime() : null;

                    if (record.getStatus() == AttendanceStatus.PRESENT) {
                        presentCount++;
                    } else if (record.getStatus() == AttendanceStatus.EXCUSED) {
                        excusedCount++;
                    }

                    ExcuseRequest excuse = excuseMap.get(record.getId());
                    if (excuse != null) {
                        excuseStatus = excuse.getStatus().name();
                    }
                } else {
                    status = "ABSENT";
                    absentCount++;
                }

                sessionAttendances.add(MyAttendanceResponse.SessionAttendance.builder()
                        .sessionId(session.getId())
                        .date(session.getSessionDate())
                        .startTime(session.getStartTime())
                        .status(status)
                        .checkInTime(checkInTime)
                        .excuseStatus(excuseStatus)
                        .build());
            }

            int totalSessions = sessions.size();
            double percentage = totalSessions > 0
                    ? ((double) (presentCount + excusedCount) / totalSessions) * 100
                    : 0;

            responses.add(MyAttendanceResponse.builder()
                    .courseCode(sectionInfo.getCourseCode())
                    .courseName(sectionInfo.getCourseName())
                    .sectionNumber(sectionInfo.getSectionNumber())
                    .totalSessions(totalSessions)
                    .presentCount(presentCount)
                    .absentCount(absentCount)
                    .excusedCount(excusedCount)
                    .attendancePercentage(Math.round(percentage * 10) / 10.0)
                    .status(getAttendanceStatusString(percentage))
                    .sessions(sessionAttendances)
                    .build());
        }

        return responses;
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

    @Override
    public List<SessionResponse> getActiveSessionsForStudent(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        // Önce user_id'den students.id'yi bul (enrollments tablosu students.id
        // kullanıyor)
        String getStudentIdSql = "SELECT id FROM students WHERE user_id = ?";
        Long studentId;
        try {
            studentId = jdbcTemplate.queryForObject(getStudentIdSql, Long.class, userId);
            log.info("📚 User {} için student_id: {}", userId, studentId);
        } catch (Exception e) {
            log.error("❌ User {} için student kaydı bulunamadı: {}", userId, e.getMessage());
            return new ArrayList<>();
        }

        if (studentId == null) {
            log.warn("⚠️ User {} için student kaydı bulunamadı", userId);
            return new ArrayList<>();
        }

        // Öğrencinin kayıtlı olduğu section'ları enrollment tablosundan çek
        String enrollmentSql = "SELECT DISTINCT section_id FROM enrollments " +
                "WHERE student_id = ? AND status = 'ENROLLED'";
        List<Long> enrolledSectionIds;
        try {
            enrolledSectionIds = jdbcTemplate.queryForList(enrollmentSql, Long.class, studentId);
            log.info("📚 Student {} için kayıtlı section sayısı: {}", studentId, enrolledSectionIds.size());
        } catch (Exception e) {
            log.error("❌ Enrollment bilgisi alınırken hata: {}", e.getMessage());
            enrolledSectionIds = new ArrayList<>();
        }

        // Eğer öğrenci hiçbir derse kayıtlı değilse boş liste dön
        if (enrolledSectionIds.isEmpty()) {
            log.info("⚠️ Student {} hiçbir derse kayıtlı görünmüyor", studentId);
            return new ArrayList<>();
        }

        // Aktif oturumları bul
        List<AttendanceSession> activeSessions = sessionRepository.findActiveSessions(
                enrolledSectionIds, SessionStatus.ACTIVE);

        log.info("🔍 Student {} için {} aktif yoklama oturumu bulundu", studentId, activeSessions.size());

        // Öğrencinin zaten yoklama verdiği oturumları ve süresi geçmiş olanları
        // filtrele
        List<SessionResponse> result = new ArrayList<>();
        for (AttendanceSession session : activeSessions) {
            // Oturum süresi geçmiş mi kontrol et
            if (!isSessionStillActive(session, now)) {
                log.debug("⏰ Oturum {} süresi geçmiş, atlanıyor", session.getId());
                continue;
            }

            // Öğrenci bu oturuma yoklama vermiş mi kontrol et (studentId kullan)
            boolean alreadyCheckedIn = recordRepository.findBySessionIdAndStudentId(
                    session.getId(), studentId).isPresent();

            if (!alreadyCheckedIn) {
                SessionResponse response = mapToSessionResponseWithCourseInfo(session);
                result.add(response);
                log.info("✅ Aktif yoklama oturumu bulundu: sessionId={}, sectionId={}",
                        session.getId(), session.getSectionId());
            } else {
                log.debug("✓ Student {} oturum {} için zaten yoklama vermiş", studentId, session.getId());
            }
        }

        log.info("📋 Student {} için toplam {} aktif yoklama oturumu döndürülüyor", studentId, result.size());
        return result;
    }

    /**
     * Oturumun hala aktif olup olmadığını kontrol eder (süresi geçmemiş)
     */
    private boolean isSessionStillActive(AttendanceSession session, LocalDateTime now) {
        // Eğer endTime yoksa, oturum süresiz sayılır ve aktif kabul edilir
        if (session.getEndTime() == null) {
            // Sadece bugünün oturumlarını göster
            return session.getSessionDate().equals(now.toLocalDate()) ||
                    session.getSessionDate().isAfter(now.toLocalDate());
        }

        // Session tarih ve saati
        LocalDateTime sessionEndDateTime = LocalDateTime.of(
                session.getSessionDate(),
                session.getEndTime());

        // Şu anki zaman session bitiş zamanından önce mi?
        return now.isBefore(sessionEndDateTime) || now.isEqual(sessionEndDateTime);
    }

    private SessionResponse mapToSessionResponseWithCourseInfo(AttendanceSession session) {
        SessionResponse response = mapToSessionResponse(session);

        // Section bilgilerini ekle
        CourseSectionInfo sectionInfo = courseSectionInfoRepository.findBySectionId(session.getSectionId());
        if (sectionInfo != null) {
            return SessionResponse.builder()
                    .id(response.getId())
                    .sectionId(response.getSectionId())
                    .date(response.getDate())
                    .startTime(response.getStartTime())
                    .endTime(response.getEndTime())
                    .latitude(response.getLatitude())
                    .longitude(response.getLongitude())
                    .geofenceRadius(response.getGeofenceRadius())
                    .qrCode(response.getQrCode())
                    .qrCodeUrl(response.getQrCodeUrl())
                    .status(response.getStatus())
                    .presentCount(response.getPresentCount())
                    .courseCode(sectionInfo.getCourseCode())
                    .courseName(sectionInfo.getCourseName())
                    .sectionNumber(sectionInfo.getSectionNumber())
                    .build();
        }

        return response;
    }

    private void validateCampusNetwork(String ipAddress) {
        if (!ipValidator.isOnCampusNetwork(ipAddress)) {
            throw new ForbiddenException("Yoklama vermek için kampüs ağına bağlı olmalısınız",
                    "NOT_ON_CAMPUS_NETWORK",
                    Map.of("ipAddress", ipAddress != null ? ipAddress : "unknown",
                            "message", "Lütfen kampüs WiFi ağına bağlanın ve tekrar deneyin"));
        }
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
        if (percentage >= 80)
            return "OK";
        if (percentage >= 70)
            return "WARNING";
        return "CRITICAL";
    }
}