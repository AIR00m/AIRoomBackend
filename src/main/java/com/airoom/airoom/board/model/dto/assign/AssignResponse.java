package com.airoom.airoom.board.model.dto.assign;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.BoardType;

import java.time.LocalDateTime;

public record AssignResponse(
        Long assignBoardNo,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String content,
        BoardType boardType

) {
    public static AssignResponse makeResponse(AssignBoard board) {
        return new AssignResponse(
                board.getAssignBoardNo(),
                board.getAssignBoardTitle(),
                board.getAssignStart(),
                board.getAssignEnd(),
                board.getAssignBoardContent(),
                BoardType.ASSIGN

        );

    }
}
