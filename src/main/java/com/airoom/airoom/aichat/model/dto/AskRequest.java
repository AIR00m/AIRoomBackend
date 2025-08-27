package com.airoom.airoom.aichat.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AskRequest {
    private String roomId;   // 옵션
    private String message;  // 필수
    // 추가: 학생/문맥 정보(교과서, 단원, 성적 등)
    private Map<String,Object> context; // 선택
}
