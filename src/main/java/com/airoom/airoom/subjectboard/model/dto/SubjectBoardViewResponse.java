package com.airoom.airoom.subjectboard.model.dto;

import com.airoom.airoom.attach.model.dto.AttachmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectBoardViewResponse {
    private Long sbNo;
    private String sbTitle;
    private String sbContent;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean hasAttachment;
    private boolean isPinned;

    private List<AttachmentResponse> attachments;
}
