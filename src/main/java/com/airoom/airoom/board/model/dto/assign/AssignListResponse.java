package com.airoom.airoom.board.model.dto.assign;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssignListResponse(
        Long assignBoardNo,
        String assignBoardTitle,
        boolean groupAssignType,
        LocalDateTime startDate,
        LocalDateTime dueDate,
        Boolean submitStatus // "true" or "false"
) {}