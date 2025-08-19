package com.airoom.airoom.subjectboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectBoardListResponse {
    private String sbTitle;
    private Long sbNo;
}
