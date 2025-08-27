package com.airoom.airoom.notification.model.dto;

import com.airoom.airoom.notification.entity.Notification;
import com.airoom.airoom.notification.entity.value.NotificationType;
import com.airoom.airoom.notification.entity.value.ReadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationDto {
    private Long notificationId;
    private String message;           // "[과제] 새로운 과제가 출제되었습니다"
    private String url;              // "/assign/student/123"
    private NotificationType notificationType;
    private LocalDateTime createdAt;
    private boolean isRead;
    private Long memberNo;

    // Notification Entity로부터 DTO 생성
    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .notificationId(notification.getNotificationNo())
                .message(notification.getNotificationType().getMessage())
                .url(notification.getNotificationUrl())
                .notificationType(notification.getNotificationType())
                .createdAt(notification.getCreatedAt())  // BaseEntity에서 상속
                .isRead(notification.getNotificationReadType() == ReadType.Y)
                .memberNo(notification.getMember().getMemberNo())
                .build();
    }

}
