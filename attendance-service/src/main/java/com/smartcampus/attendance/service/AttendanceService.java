package com.smartcampus.attendance.service;

import com.smartcampus.attendance.dto.request.CheckInQrRequest;
import com.smartcampus.attendance.dto.request.CheckInRequest;
import com.smartcampus.attendance.dto.request.CreateSessionRequest;
import com.smartcampus.attendance.dto.response.*;
import com.smartcampus.attendance.entity.SessionStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    SessionResponse createSession(Long instructorId, CreateSessionRequest request);

    SessionResponse getSession(Long sessionId);

    SessionResponse closeSession(Long instructorId, Long sessionId);

    PageResponse<SessionResponse> getMySessions(Long instructorId, Long sectionId, SessionStatus status,
                                                  LocalDate startDate, LocalDate endDate, Pageable pageable);

    AttendanceReportResponse getAttendanceReport(Long instructorId, Long sectionId, LocalDate startDate, LocalDate endDate);

    CheckInResponse checkIn(Long studentId, Long sessionId, CheckInRequest request, String ipAddress);

    CheckInResponse checkInWithQr(Long studentId, Long sessionId, CheckInQrRequest request, String ipAddress);

    List<MyAttendanceResponse> getMyAttendance(Long studentId, String semester, Integer year);

    SessionResponse refreshQrCode(Long instructorId, Long sessionId);
}
