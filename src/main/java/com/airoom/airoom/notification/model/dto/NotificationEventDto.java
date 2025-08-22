package com.airoom.airoom.notification.model.dto;

import com.airoom.airoom.notification.entity.value.NotificationType;
import com.airoom.airoom.notification.entity.value.ReadType;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

public record NotificationEventDto(

       String notificationContent,
       String notificationUrl,
       NotificationType notificationType,
       ReadType notificationReadType,
       List<Long>  targetMemberNos
) {
}
