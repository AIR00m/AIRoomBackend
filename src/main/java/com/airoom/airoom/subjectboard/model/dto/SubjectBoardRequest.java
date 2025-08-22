package com.airoom.airoom.subjectboard.model.dto;

import com.airoom.airoom.attach.model.dto.AttachmentDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectBoardRequest {
    private String title;
    private String content;
    private boolean focusType;
    private Long classroomNo;
    private Long classroomTeacherNo;

    private List<Long> deleteAttachments;
}
