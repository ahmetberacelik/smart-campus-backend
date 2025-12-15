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

    @Value("${attendance.gps-accuracy-threshold:50}")
    private double gpsAccuracyThreshold;

    public SpoofingResult detectSpoofing(Double latitude, Double longitude, Double accuracy,
                                          Boolean isMockLocation, Optional<AttendanceRecord> lastRecord) {
        if (Boolean.TRUE.equals(isMockLocation)) {
            return SpoofingResult.flagged("MOCK_LOCATION_ENABLED",
                    "Cihazda mock location özelliği tespit edildi");
        }

        if (accuracy != null && accuracy > gpsAccuracyThreshold) {
            return SpoofingResult.flagged("LOW_GPS_ACCURACY",
                    String.format("GPS doğruluğu çok düşük: %.1f metre (maksimum: %.1f metre)",
                            accuracy, gpsAccuracyThreshold));
        }

        if (lastRecord.isPresent()) {
            AttendanceRecord last = lastRecord.get();
            if (last.getLatitude() != null && last.getLongitude() != null && last.getCheckInTime() != null) {
                double distance = gpsUtils.calculateDistance(
                        last.getLatitude(), last.getLongitude(), latitude, longitude);

                long secondsElapsed = Duration.between(last.getCheckInTime(), LocalDateTime.now()).getSeconds();

                if (secondsElapsed > 0) {
                    double maxPossibleDistance = secondsElapsed * maxWalkingSpeed;

                    if (distance > maxPossibleDistance && distance > 100) {
                        return SpoofingResult.flagged("IMPOSSIBLE_TRAVEL",
                                String.format("Son konumunuzdan bu konuma belirtilen sürede ulaşmanız mümkün değil " +
                                        "(Mesafe: %.0f m, Süre: %d sn, Maks mümkün: %.0f m)",
                                        distance, secondsElapsed, maxPossibleDistance));
                    }
                }
            }
        }

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
