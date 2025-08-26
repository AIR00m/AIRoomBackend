package com.airoom.airoom.board.model.dto.assign;

public record AssignListResponse(
        Long assignBoardNo,
        String assignBoardTitle,
        boolean groupAssignType,
        String startDate,
        String dueDate,
        String submitStatus // "true" or "false"
) {}