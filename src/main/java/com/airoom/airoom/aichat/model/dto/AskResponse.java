package com.airoom.airoom.aichat.model.dto;
import lombok.*;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class AskResponse {
    private String answer;
    private List<SourceDto> sources;
}
