package com.smartcampus.academic.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSlot {
    private String day;
    private String startTime;
    private String endTime;

    public boolean overlapsWith(ScheduleSlot other) {
        if (!this.day.equalsIgnoreCase(other.day)) {
            return false;
        }
        int thisStart = parseTime(this.startTime);
        int thisEnd = parseTime(this.endTime);
        int otherStart = parseTime(other.startTime);
        int otherEnd = parseTime(other.endTime);

        return thisStart < otherEnd && otherStart < thisEnd;
    }

    private int parseTime(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
