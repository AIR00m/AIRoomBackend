package com.airoom.airoom.board.model.dto.assign;

public record AssignListResponse(
        Long id,
        String title,
        boolean groupAssignType,
        String startDate,
        String dueDate,
        String submitStatus // "true" or "false"
) {}