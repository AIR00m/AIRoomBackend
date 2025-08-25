package com.airoom.airoom.board.model.dto.group;

import java.time.LocalDateTime;

public record GroupBoardResponse(
        String groupBoardTitle,
        String groupBoardContent,
        String memberName,
        LocalDateTime createdAt,
        String s3Key,
        String originalName
)
{
}
