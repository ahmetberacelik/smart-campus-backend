package com.smartcampus.attendance.scheduler;

import com.smartcampus.attendance.entity.AttendanceRecord;
import com.smartcampus.attendance.entity.AttendanceSession;
import com.smartcampus.attendance.entity.AttendanceStatus;
import com.smartcampus.attendance.repository.AttendanceRecordRepository;
import com.smartcampus.attendance.repository.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbsenceWarningScheduler {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;

    private static final int WARNING_THRESHOLD_PERCENT = 30;
    private static final int CRITICAL_THRESHOLD_PERCENT = 50;

    @Scheduled(cron = "0 0 8 * * MON")
    public void checkWeeklyAbsenceWarnings() {
        log.info("Haftalık devamsızlık kontrolü başlatıldı...");
        
        List<AttendanceSession> allSessions = sessionRepository.findAll();
        
        Map<Long, List<AttendanceSession>> sessionsBySectionId = allSessions.stream()
                .collect(Collectors.groupingBy(AttendanceSession::getSectionId));
        
        int warningCount = 0;
        int criticalCount = 0;
        
        for (Map.Entry<Long, List<AttendanceSession>> entry : sessionsBySectionId.entrySet()) {
            Long sectionId = entry.getKey();
            List<AttendanceSession> sectionSessions = entry.getValue();
            
            if (sectionSessions.isEmpty()) continue;
            
            int totalSessions = sectionSessions.size();
            List<Long> sessionIds = sectionSessions.stream()
                    .map(AttendanceSession::getId)
                    .collect(Collectors.toList());
            
            List<Long> studentIds = recordRepository.findDistinctStudentIdsBySectionId(sectionId);
            
            for (Long studentId : studentIds) {
                AbsenceStats stats = calculateAbsenceStats(studentId, sessionIds, totalSessions);
                
                if (stats.absencePercent >= CRITICAL_THRESHOLD_PERCENT) {
                    logCriticalWarning(studentId, sectionId, stats);
                    criticalCount++;
                } else if (stats.absencePercent >= WARNING_THRESHOLD_PERCENT) {
                    logWarning(studentId, sectionId, stats);
                    warningCount++;
                }
            }
        }
        
        log.info("Haftalık devamsızlık kontrolü tamamlandı. Uyarı: {}, Kritik: {}", 
                warningCount, criticalCount);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void autoCloseExpiredSessions() {
        log.info("Süresi geçen oturumlar kontrol ediliyor...");
        
        List<AttendanceSession> allSessions = sessionRepository.findAll();
        LocalDate today = LocalDate.now();
        int closedCount = 0;
        
        for (AttendanceSession session : allSessions) {
            if (session.getSessionDate().isBefore(today) && 
                session.getStatus() == com.smartcampus.attendance.entity.SessionStatus.ACTIVE) {
                session.setStatus(com.smartcampus.attendance.entity.SessionStatus.CLOSED);
                sessionRepository.save(session);
                closedCount++;
            }
        }
        
        log.info("{} adet oturum otomatik olarak kapatıldı", closedCount);
    }

    private AbsenceStats calculateAbsenceStats(Long studentId, List<Long> sessionIds, int totalSessions) {
        List<AttendanceRecord> records = recordRepository.findBySessionIdsAndStudentId(sessionIds, studentId);
        
        long absentCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT)
                .count();
        
        long excusedCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.EXCUSED)
                .count();
        
        long presentCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                .count();
        
        long lateCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.LATE)
                .count();
        
        double absencePercent = totalSessions > 0 ? 
                (double) absentCount / totalSessions * 100 : 0;
        
        return new AbsenceStats(
                (int) absentCount, 
                (int) excusedCount, 
                (int) presentCount, 
                (int) lateCount,
                totalSessions,
                absencePercent
        );
    }

    private void logWarning(Long studentId, Long sectionId, AbsenceStats stats) {
        log.warn("DEVAMSIZLIK UYARISI - Öğrenci: {}, Section: {}, " +
                "Devamsız: {}/{} ({}%), Mazeretli: {}", 
                studentId, sectionId, 
                stats.absentCount, stats.totalSessions, 
                String.format("%.1f", stats.absencePercent),
                stats.excusedCount);
    }

    private void logCriticalWarning(Long studentId, Long sectionId, AbsenceStats stats) {
        log.error("KRİTİK DEVAMSIZLIK - Öğrenci: {}, Section: {}, " +
                "Devamsız: {}/{} ({}%), Mazeretli: {} - DERS KALMA RİSKİ!", 
                studentId, sectionId, 
                stats.absentCount, stats.totalSessions, 
                String.format("%.1f", stats.absencePercent),
                stats.excusedCount);
    }

    private record AbsenceStats(
            int absentCount,
            int excusedCount,
            int presentCount,
            int lateCount,
            int totalSessions,
            double absencePercent
    ) {}
}
