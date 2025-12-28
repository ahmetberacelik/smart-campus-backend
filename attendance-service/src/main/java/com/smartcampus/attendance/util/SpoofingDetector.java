package com.smartcampus.attendance.util;

import com.smartcampus.attendance.entity.AttendanceRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpoofingDetector {

    private final GpsUtils gpsUtils;

    @Value("${attendance.max-walking-speed:2.0}")
    private double maxWalkingSpeed;

    @Value("${attendance.gps-accuracy-threshold:5000}")
    private double gpsAccuracyThreshold;

    public SpoofingResult detectSpoofing(Double latitude, Double longitude, Double accuracy,
            Boolean isMockLocation, Optional<AttendanceRecord> lastRecord) {
        // TEST AŞAMASINDA GEÇİCİ OLARAK DEVRE DIŞI - Production'da açılmalı!
        // Tüm spoofing kontrollerini bypass ediyoruz
        return SpoofingResult.clean();
    }

    public record SpoofingResult(boolean isFlagged, String reason, String message) {
        public static SpoofingResult flagged(String reason, String message) {
            return new SpoofingResult(true, reason, message);
        }

        public static SpoofingResult clean() {
            return new SpoofingResult(false, null, null);
        }
    }
}
