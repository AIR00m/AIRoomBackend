package com.airoom.airoom.aichat.model.dto;

import lombok.Data;

@Data
public class AskRequest {
    private String roomId;   // 옵션
    private String message;  // 필수
}
