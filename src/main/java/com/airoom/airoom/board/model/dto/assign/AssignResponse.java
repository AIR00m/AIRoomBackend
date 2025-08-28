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
        boolean isGroupAssignType,
        Long homeworkBoardNo,
        String homeworkBoardContent
) { }
