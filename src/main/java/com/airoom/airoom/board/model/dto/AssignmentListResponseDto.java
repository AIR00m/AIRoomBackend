package com.airoom.airoom.board.model.dto;

public record AssignmentListResponseDto(
        Long id,
        String title,
        boolean groupAssignType,
        String startDate,
        String dueDate,
        String submitStatus // "true" or "false"
) {}