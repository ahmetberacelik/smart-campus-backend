package com.smartcampus.attendance.repository;

import com.smartcampus.attendance.dto.response.CourseSectionInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CourseSectionInfoRepository {

    private final JdbcTemplate jdbcTemplate;

    public CourseSectionInfoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CourseSectionInfo> rowMapper = (rs, rowNum) -> CourseSectionInfo.builder()
            .sectionId(rs.getLong("section_id"))
            .courseCode(rs.getString("course_code"))
            .courseName(rs.getString("course_name"))
            .sectionNumber(rs.getString("section_number"))
            .semester(rs.getString("semester"))
            .year(rs.getInt("year"))
            .build();

    public CourseSectionInfo findBySectionId(Long sectionId) {
        String sql = "SELECT cs.id as section_id, c.code as course_code, c.name as course_name, " +
                "cs.section_number, cs.semester, cs.year " +
                "FROM course_sections cs " +
                "JOIN courses c ON cs.course_id = c.id " +
                "WHERE cs.id = ?";
        List<CourseSectionInfo> results = jdbcTemplate.query(sql, rowMapper, sectionId);
        return results.isEmpty() ? null : results.get(0);
    }

    public Map<Long, CourseSectionInfo> findBySectionIds(List<Long> sectionIds) {
        if (sectionIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = sectionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT cs.id as section_id, c.code as course_code, c.name as course_name, " +
                "cs.section_number, cs.semester, cs.year " +
                "FROM course_sections cs " +
                "JOIN courses c ON cs.course_id = c.id " +
                "WHERE cs.id IN (" + placeholders + ")";
        List<CourseSectionInfo> results = jdbcTemplate.query(sql, rowMapper, sectionIds.toArray());
        return results.stream().collect(Collectors.toMap(CourseSectionInfo::getSectionId, info -> info));
    }
}
