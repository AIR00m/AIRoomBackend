package com.airoom.airoom.subjectboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectBoardListResponse {
    private Long sbNo;
    private String sbTitle;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean hasAttachment;
    private boolean isPinned;
}
