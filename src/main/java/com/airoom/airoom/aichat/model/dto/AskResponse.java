package com.airoom.airoom.aichat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor
@AllArgsConstructor
public class AskResponse {
    private String answer;
    private List<SourceDto> sources = List.of();
    private List<SourceIndex> indices = List.of(); // 문서 i ↔ id/title 매핑(프론트가 "문서 1=경찰" 표시 가능)

    // 기존 코드 호환용(2-파라미터) 생성자 유지
    public AskResponse(String answer, List<SourceDto> sources) {
        this.answer = answer;
        this.sources = (sources == null ? List.of() : sources);
        this.indices = List.of();
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class SourceIndex {
        private int i;         // 1-based
        private String id;
        private String title;
    }
}
