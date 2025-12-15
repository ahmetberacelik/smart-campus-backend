package com.smartcampus.attendance.service;

public interface NotificationService {

    void sendAbsenceWarningEmail(String to, String studentName, String courseCode, 
            String courseName, int absentCount, int totalSessions, double absencePercent);

    void sendCriticalAbsenceEmail(String to, String studentName, String courseCode,
            String courseName, int absentCount, int totalSessions, double absencePercent);
}
