package com.airoom.airoom.board.model.dto.assign;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.BoardType;

import java.time.LocalDateTime;

public record AssignTeacherResponse(
        Long assignBoardNo,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String content,
        BoardType boardType,
        boolean isGroup
) {
    public static AssignTeacherResponse makeResponse(AssignBoard board, boolean isGroup) {
        return new AssignTeacherResponse(
                board.getAssignBoardNo(),
                board.getAssignBoardTitle(),
                board.getAssignStart(),
                board.getAssignEnd(),
                board.getAssignBoardContent(),
                BoardType.ASSIGN,
                isGroup

        );

    }
}
