package com.airoom.airoom.notification.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// NotificationResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long notificationNo;
    private String notificationType;
    private String message;
    private String redirectUrl;
    private Boolean isRead;
    private String createdTime;
    private String notificationUrl;

}
