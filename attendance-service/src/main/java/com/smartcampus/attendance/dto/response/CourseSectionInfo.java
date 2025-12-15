package com.smartcampus.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSectionInfo {
    private Long sectionId;
    private String courseCode;
    private String courseName;
    private String sectionNumber;
    private String semester;
    private Integer year;
}
