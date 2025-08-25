package com.airoom.airoom.board.model.dto.group;

import java.time.LocalDateTime;


public record GroupBoardsResponse(
        Long groupBoardNo,
        String groupBoardTitle,
        String groupBoardContent,
        String memberName,
        LocalDateTime createdAt
)
{
}
