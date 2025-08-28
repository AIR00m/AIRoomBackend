package com.airoom.airoom.notification.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// NotificationListResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponseDto {
    private List<NotificationResponseDto> notifications;
    private Boolean hasMore;
    private Integer totalCount;
}
