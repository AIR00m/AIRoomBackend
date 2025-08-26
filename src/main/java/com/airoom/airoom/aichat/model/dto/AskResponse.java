package com.airoom.airoom.aichat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data @AllArgsConstructor
public class AskResponse {
    private String answer;
    private List<SourceDto> sources;
}
