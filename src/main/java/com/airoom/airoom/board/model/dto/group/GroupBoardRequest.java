package com.airoom.airoom.board.model.dto.group;

import java.time.LocalDateTime;

public record GroupBoardRequest(
        String groupBoardTitle,
        String groupBoardContent,
        LocalDateTime createdAt
)
{ }
