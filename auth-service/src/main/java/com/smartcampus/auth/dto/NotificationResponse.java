package com.smartcampus.auth.dto;

import com.smartcampus.auth.entity.NotificationCategory;
import com.smartcampus.auth.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private NotificationCategory category;
    private String title;
    private String message;
    private String dataJson;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
