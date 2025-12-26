package com.smartcampus.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {
    // Email Preferences
    private Boolean emailAcademic;
    private Boolean emailAttendance;
    private Boolean emailMeal;
    private Boolean emailEvent;
    private Boolean emailPayment;
    private Boolean emailSystem;

    // Push Preferences
    private Boolean pushAcademic;
    private Boolean pushAttendance;
    private Boolean pushMeal;
    private Boolean pushEvent;
    private Boolean pushPayment;
    private Boolean pushSystem;

    // SMS Preferences
    private Boolean smsAttendance;
    private Boolean smsPayment;
}
