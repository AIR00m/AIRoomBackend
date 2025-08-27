package com.airoom.airoom.aichat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor
public class SourceDto {
    private String id;
    private double score;
    private Map<String, Object> payload;
}
