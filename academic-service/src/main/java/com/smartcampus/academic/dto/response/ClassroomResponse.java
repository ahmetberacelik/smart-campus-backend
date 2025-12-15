package com.smartcampus.academic.dto.response;

import com.smartcampus.academic.entity.Classroom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomResponse {

    private Long id;
    private String building;
    private String roomNumber;
    private String fullName;
    private Integer capacity;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String featuresJson;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static ClassroomResponse from(Classroom classroom) {
        return ClassroomResponse.builder()
                .id(classroom.getId())
                .building(classroom.getBuilding())
                .roomNumber(classroom.getRoomNumber())
                .fullName(classroom.getFullName())
                .capacity(classroom.getCapacity())
                .latitude(classroom.getLatitude())
                .longitude(classroom.getLongitude())
                .featuresJson(classroom.getFeaturesJson())
                .isActive(classroom.getIsActive())
                .createdAt(classroom.getCreatedAt())
                .build();
    }
}
