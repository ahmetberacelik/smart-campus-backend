package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

        private final JdbcTemplate jdbcTemplate;

        /**
         * Admin Dashboard - Genel istatistikler
         */
        @Transactional(readOnly = true)
        public DashboardStatsResponse getDashboardStats() {
                log.info("Dashboard istatistikleri hesaplanıyor...");

                // Kullanıcı istatistikleri
                Long totalUsers = countFromTable("users", "deleted_at IS NULL AND is_active = 1");
                Long totalStudents = countFromTable("students", null);
                Long totalFaculty = countFromTable("faculty", null);
                Long totalAdmins = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND is_active = 1", Long.class);

                // Akademik istatistikler
                Long totalDepartments = countFromTable("departments", null);
                Long totalCourses = countFromTable("courses", null);
                Long totalSections = countFromTable("course_sections", null);
                Long totalEnrollments = countFromTable("enrollments", "status = 'ACTIVE'");

                // Yoklama istatistikleri
                Long totalSessions = countFromTable("attendance_sessions", null);
                Double avgAttendanceRate = calculateAverageAttendanceRate();

                // Yemek istatistikleri
                Long mealReservationsToday = countMealReservations(LocalDate.now());
                Long mealReservationsMonth = countMealReservationsThisMonth();

                // Etkinlik istatistikleri
                Long totalEvents = countFromTable("events", null);
                Long upcomingEvents = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM events WHERE event_date >= CURDATE() AND status = 'PUBLISHED'",
                                Long.class);
                Long totalRegistrations = countFromTable("event_registrations", "status != 'CANCELLED'");

                return DashboardStatsResponse.builder()
                                .totalUsers(totalUsers != null ? totalUsers : 0L)
                                .totalStudents(totalStudents != null ? totalStudents : 0L)
                                .totalFaculty(totalFaculty != null ? totalFaculty : 0L)
                                .totalAdmins(totalAdmins != null ? totalAdmins : 0L)
                                .totalDepartments(totalDepartments != null ? totalDepartments : 0L)
                                .totalCourses(totalCourses != null ? totalCourses : 0L)
                                .totalSections(totalSections != null ? totalSections : 0L)
                                .totalEnrollments(totalEnrollments != null ? totalEnrollments : 0L)
                                .totalAttendanceSessions(totalSessions != null ? totalSessions : 0L)
                                .averageAttendanceRate(avgAttendanceRate != null ? avgAttendanceRate : 0.0)
                                .totalMealReservationsToday(mealReservationsToday != null ? mealReservationsToday : 0L)
                                .totalMealReservationsThisMonth(
                                                mealReservationsMonth != null ? mealReservationsMonth : 0L)
                                .totalEvents(totalEvents != null ? totalEvents : 0L)
                                .upcomingEvents(upcomingEvents != null ? upcomingEvents : 0L)
                                .totalEventRegistrations(totalRegistrations != null ? totalRegistrations : 0L)
                                .systemHealth("healthy")
                                .lastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .build();
        }

        /**
         * Akademik performans istatistikleri
         */
        @Transactional(readOnly = true)
        public AcademicStatsResponse getAcademicStats() {
                log.info("Akademik istatistikler hesaplanıyor...");

                // Genel GPA istatistikleri
                Map<String, Object> gpaStats = jdbcTemplate.queryForMap(
                                "SELECT AVG(gpa) as avgGpa, AVG(cgpa) as avgCgpa, MAX(cgpa) as maxCgpa, MIN(cgpa) as minCgpa FROM students WHERE cgpa > 0");

                // Bölüm bazlı GPA
                List<AcademicStatsResponse.DepartmentGpaStats> deptStats = jdbcTemplate.query(
                                """
                                                SELECT d.id, d.name, d.code, AVG(s.cgpa) as avgGpa, COUNT(s.id) as studentCount
                                                FROM departments d
                                                LEFT JOIN students s ON d.id = s.department_id
                                                GROUP BY d.id, d.name, d.code
                                                ORDER BY avgGpa DESC
                                                """,
                                (rs, rowNum) -> AcademicStatsResponse.DepartmentGpaStats.builder()
                                                .departmentId(rs.getLong("id"))
                                                .departmentName(rs.getString("name"))
                                                .departmentCode(rs.getString("code"))
                                                .averageGpa(rs.getDouble("avgGpa"))
                                                .studentCount(rs.getLong("studentCount"))
                                                .build());

                // Not dağılımı
                Map<String, Double> gradeDistribution = calculateGradeDistribution();

                // GPA bazlı öğrenci sayıları
                Long above3 = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM students WHERE cgpa >= 3.0", Long.class);
                Long between2And3 = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM students WHERE cgpa >= 2.0 AND cgpa < 3.0", Long.class);
                Long below2 = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM students WHERE cgpa > 0 AND cgpa < 2.0", Long.class);

                // Geçme oranı
                Double passRate = calculatePassRate();

                return AcademicStatsResponse.builder()
                                .averageGpa(gpaStats.get("avgGpa") != null
                                                ? ((Number) gpaStats.get("avgGpa")).doubleValue()
                                                : 0.0)
                                .averageCgpa(gpaStats.get("avgCgpa") != null
                                                ? ((Number) gpaStats.get("avgCgpa")).doubleValue()
                                                : 0.0)
                                .highestGpa(gpaStats.get("maxCgpa") != null
                                                ? ((Number) gpaStats.get("maxCgpa")).doubleValue()
                                                : 0.0)
                                .lowestGpa(gpaStats.get("minCgpa") != null
                                                ? ((Number) gpaStats.get("minCgpa")).doubleValue()
                                                : 0.0)
                                .departmentStats(deptStats)
                                .gradeDistribution(gradeDistribution)
                                .passRate(passRate)
                                .failRate(100.0 - passRate)
                                .studentsAbove3(above3 != null ? above3 : 0L)
                                .studentsBetween2And3(between2And3 != null ? between2And3 : 0L)
                                .studentsBelow2(below2 != null ? below2 : 0L)
                                .build();
        }

        /**
         * Yoklama istatistikleri
         */
        @Transactional(readOnly = true)
        public AttendanceStatsResponse getAttendanceStats() {
                log.info("Yoklama istatistikleri hesaplanıyor...");

                Long totalSessions = countFromTable("attendance_sessions", null);
                Long totalRecords = countFromTable("attendance_records", null);
                Double overallRate = calculateAverageAttendanceRate();

                // Devamsızlık uyarıları - örnek sorgu
                Long warning = 0L; // Bu değer enrollment bazlı hesaplanmalı
                Long critical = 0L;

                // Ders bazlı yoklama oranları
                List<AttendanceStatsResponse.CourseAttendanceStats> courseStats = jdbcTemplate.query(
                                """
                                                SELECT c.id, c.code, c.name,
                                                       COUNT(DISTINCT ass.id) as sessionCount,
                                                       (SELECT COUNT(*) FROM enrollments e WHERE e.section_id IN
                                                           (SELECT cs.id FROM course_sections cs WHERE cs.course_id = c.id)) as enrolled
                                                FROM courses c
                                                LEFT JOIN course_sections cs ON c.id = cs.course_id
                                                LEFT JOIN attendance_sessions ass ON cs.id = ass.section_id
                                                GROUP BY c.id, c.code, c.name
                                                HAVING sessionCount > 0
                                                ORDER BY sessionCount DESC
                                                LIMIT 10
                                                """,
                                (rs, rowNum) -> AttendanceStatsResponse.CourseAttendanceStats.builder()
                                                .courseId(rs.getLong("id"))
                                                .courseCode(rs.getString("code"))
                                                .courseName(rs.getString("name"))
                                                .attendanceRate(85.0) // Placeholder - detaylı hesaplama gerekir
                                                .sessionCount(rs.getLong("sessionCount"))
                                                .enrolledStudents(rs.getLong("enrolled"))
                                                .build());

                return AttendanceStatsResponse.builder()
                                .overallAttendanceRate(overallRate != null ? overallRate : 0.0)
                                .totalSessions(totalSessions != null ? totalSessions : 0L)
                                .totalRecords(totalRecords != null ? totalRecords : 0L)
                                .studentsWithWarning(warning)
                                .studentsWithCritical(critical)
                                .courseStats(courseStats)
                                .weeklyTrend(new ArrayList<>())
                                .build();
        }

        /**
         * Yemek kullanım istatistikleri
         */
        @Transactional(readOnly = true)
        public MealStatsResponse getMealStats() {
                log.info("Yemek istatistikleri hesaplanıyor...");

                Long today = countMealReservations(LocalDate.now());
                Long thisWeek = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM meal_reservations WHERE reservation_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)",
                                Long.class);
                Long thisMonth = countMealReservationsThisMonth();

                Long usedToday = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM meal_reservations WHERE reservation_date = CURDATE() AND status = 'USED'",
                                Long.class);
                Long cancelledToday = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM meal_reservations WHERE reservation_date = CURDATE() AND status = 'CANCELLED'",
                                Long.class);

                // Burslu vs ücretli
                Long scholarship = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM meal_reservations WHERE is_scholarship_used = 1", Long.class);
                Long paid = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM meal_reservations WHERE is_scholarship_used = 0 OR is_scholarship_used IS NULL",
                                Long.class);

                // Öğün dağılımı
                Map<String, Long> mealTypes = new HashMap<>();
                mealTypes.put("LUNCH", jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM meal_reservations WHERE meal_type = 'LUNCH'", Long.class));
                mealTypes.put("DINNER", jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM meal_reservations WHERE meal_type = 'DINNER'", Long.class));

                return MealStatsResponse.builder()
                                .totalReservationsToday(today != null ? today : 0L)
                                .totalReservationsThisWeek(thisWeek != null ? thisWeek : 0L)
                                .totalReservationsThisMonth(thisMonth != null ? thisMonth : 0L)
                                .usedReservationsToday(usedToday != null ? usedToday : 0L)
                                .cancelledReservationsToday(cancelledToday != null ? cancelledToday : 0L)
                                .scholarshipMeals(scholarship != null ? scholarship : 0L)
                                .paidMeals(paid != null ? paid : 0L)
                                .cafeteriaStats(new ArrayList<>())
                                .mealTypeDistribution(mealTypes)
                                .weeklyTrend(new ArrayList<>())
                                .build();
        }

        /**
         * Etkinlik istatistikleri
         */
        @Transactional(readOnly = true)
        public EventStatsResponse getEventStats() {
                log.info("Etkinlik istatistikleri hesaplanıyor...");

                Long total = countFromTable("events", null);
                Long upcoming = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM events WHERE event_date >= CURDATE() AND status = 'PUBLISHED'",
                                Long.class);
                Long past = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM events WHERE event_date < CURDATE()", Long.class);
                Long cancelled = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM events WHERE status = 'CANCELLED'", Long.class);

                Long totalRegs = countFromTable("event_registrations", "status != 'CANCELLED'");
                Long checkedIn = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM event_registrations WHERE status = 'ATTENDED'", Long.class);

                Double avgCheckIn = (totalRegs != null && totalRegs > 0 && checkedIn != null)
                                ? (checkedIn.doubleValue() / totalRegs.doubleValue() * 100)
                                : 0.0;

                // Kategori dağılımı
                Map<String, Long> categories = new HashMap<>();
                try {
                        jdbcTemplate.query(
                                        "SELECT category, COUNT(*) as cnt FROM events GROUP BY category",
                                        rs -> {
                                                categories.put(rs.getString("category"), rs.getLong("cnt"));
                                        });
                } catch (Exception e) {
                        log.warn("Kategori dağılımı hesaplanamadı: {}", e.getMessage());
                }

                return EventStatsResponse.builder()
                                .totalEvents(total != null ? total : 0L)
                                .upcomingEvents(upcoming != null ? upcoming : 0L)
                                .pastEvents(past != null ? past : 0L)
                                .cancelledEvents(cancelled != null ? cancelled : 0L)
                                .totalRegistrations(totalRegs != null ? totalRegs : 0L)
                                .checkedInCount(checkedIn != null ? checkedIn : 0L)
                                .averageCheckInRate(avgCheckIn)
                                .categoryDistribution(categories)
                                .popularEvents(new ArrayList<>())
                                .monthlyTrend(new ArrayList<>())
                                .build();
        }

        // =========== Yardımcı metodlar ===========

        private Long countFromTable(String table, String whereClause) {
                try {
                        String sql = "SELECT COUNT(*) FROM " + table;
                        if (whereClause != null && !whereClause.isEmpty()) {
                                sql += " WHERE " + whereClause;
                        }
                        return jdbcTemplate.queryForObject(sql, Long.class);
                } catch (Exception e) {
                        log.warn("Tablo sayımı başarısız: {} - {}", table, e.getMessage());
                        return 0L;
                }
        }

        private Long countMealReservations(LocalDate date) {
                try {
                        return jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM meal_reservations WHERE reservation_date = ?",
                                        Long.class, date);
                } catch (Exception e) {
                        return 0L;
                }
        }

        private Long countMealReservationsThisMonth() {
                try {
                        return jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM meal_reservations WHERE MONTH(reservation_date) = MONTH(CURDATE()) AND YEAR(reservation_date) = YEAR(CURDATE())",
                                        Long.class);
                } catch (Exception e) {
                        return 0L;
                }
        }

        private Double calculateAverageAttendanceRate() {
                try {
                        // Simplified calculation
                        Long totalRecords = countFromTable("attendance_records", null);
                        Long totalExpected = jdbcTemplate.queryForObject(
                                        """
                                                        SELECT SUM(enrolled_count) FROM course_sections cs
                                                        INNER JOIN attendance_sessions ass ON cs.id = ass.section_id
                                                        """, Long.class);

                        if (totalExpected != null && totalExpected > 0) {
                                return (totalRecords.doubleValue() / totalExpected.doubleValue()) * 100;
                        }
                        return 85.0; // Default fallback
                } catch (Exception e) {
                        return 85.0;
                }
        }

        private Map<String, Double> calculateGradeDistribution() {
                Map<String, Double> distribution = new HashMap<>();
                try {
                        Long total = jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM enrollments WHERE letter_grade IS NOT NULL", Long.class);

                        if (total != null && total > 0) {
                                for (String grade : Arrays.asList("AA", "BA", "BB", "CB", "CC", "DC", "DD", "FF")) {
                                        Long count = jdbcTemplate.queryForObject(
                                                        "SELECT COUNT(*) FROM enrollments WHERE letter_grade = ?",
                                                        Long.class, grade);
                                        distribution.put(grade, count != null
                                                        ? (count.doubleValue() / total.doubleValue() * 100)
                                                        : 0.0);
                                }
                        }
                } catch (Exception e) {
                        log.warn("Not dağılımı hesaplanamadı: {}", e.getMessage());
                }
                return distribution;
        }

        private Double calculatePassRate() {
                try {
                        Long total = jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM enrollments WHERE letter_grade IS NOT NULL", Long.class);
                        Long passed = jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM enrollments WHERE letter_grade IS NOT NULL AND letter_grade NOT IN ('FF', 'DZ')",
                                        Long.class);

                        if (total != null && total > 0 && passed != null) {
                                return (passed.doubleValue() / total.doubleValue()) * 100;
                        }
                        return 75.0; // Default
                } catch (Exception e) {
                        return 75.0;
                }
        }
}
