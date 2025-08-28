package com.airoom.airoom.notification.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListRequestDto {
    private String memberId;
    private Integer offset;
    private Integer limit;
}





