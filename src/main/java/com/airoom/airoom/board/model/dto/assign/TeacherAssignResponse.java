package com.airoom.airoom.board.model.dto.assign;

import com.airoom.airoom.board.BoardType;

import java.time.LocalDateTime;

public record TeacherAssignResponse(
        Long assignBoardNo,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String content,
        BoardType boardType

) {
}
