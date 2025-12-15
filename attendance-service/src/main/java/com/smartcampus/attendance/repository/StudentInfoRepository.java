package com.smartcampus.attendance.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class StudentInfoRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentInfoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long studentId;
        private Long userId;
        private String email;
        private String fullName;
        private String studentNumber;
    }

    private final RowMapper<StudentInfo> rowMapper = (rs, rowNum) -> StudentInfo.builder()
            .studentId(rs.getLong("student_id"))
            .userId(rs.getLong("user_id"))
            .email(rs.getString("email"))
            .fullName(rs.getString("full_name"))
            .studentNumber(rs.getString("student_number"))
            .build();

    public StudentInfo findByStudentId(Long studentId) {
        String sql = "SELECT s.id as student_id, s.user_id, u.email, " +
                "CONCAT(u.first_name, ' ', u.last_name) as full_name, s.student_number " +
                "FROM students s " +
                "JOIN users u ON s.user_id = u.id " +
                "WHERE s.id = ?";
        List<StudentInfo> results = jdbcTemplate.query(sql, rowMapper, studentId);
        return results.isEmpty() ? null : results.get(0);
    }

    public Map<Long, StudentInfo> findByStudentIds(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = studentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT s.id as student_id, s.user_id, u.email, " +
                "CONCAT(u.first_name, ' ', u.last_name) as full_name, s.student_number " +
                "FROM students s " +
                "JOIN users u ON s.user_id = u.id " +
                "WHERE s.id IN (" + placeholders + ")";
        List<StudentInfo> results = jdbcTemplate.query(sql, rowMapper, studentIds.toArray());
        return results.stream().collect(Collectors.toMap(StudentInfo::getStudentId, info -> info));
    }
}
