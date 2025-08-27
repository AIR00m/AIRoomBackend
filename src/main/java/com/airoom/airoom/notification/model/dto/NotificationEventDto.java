package com.airoom.airoom.notification.model.dto;

import com.airoom.airoom.notification.entity.value.NotificationType;

import java.sql.Timestamp;
import java.util.List;

public record NotificationEventDto(

       String notificationUrl, //NotificationType.location
       NotificationType notificationType, // LocationUrl+msg
       List<Long>  targetMemberNos
) {
}
