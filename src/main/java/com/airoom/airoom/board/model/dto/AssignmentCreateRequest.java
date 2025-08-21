package com.airoom.airoom.board.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AssignmentCreateRequest(
        AssignBoard assignBoard,
        List<AttachmentFile> attachmentFile,
        List<AssignTarget> assignTargets
) {
    public record AssignBoard(
            Long classroomTeacherNo, // 신규 추가: 교사 식별자
            Long classroomNo,
            String assignBoardTitle,
            String assignBoardContent,
            LocalDateTime assignStart,
            LocalDateTime assignEnd
    ) {}

    public record AttachmentFile(
            String originalName,
            String boardType                   // "ASSIGN" 고정값
    ) {}

    public record AssignTarget(
            Long targetNo,
            Boolean groupAssignType
    ) {}
}
