package com.airoom.airoom.board.model.dto.comment;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SaveCommentResponse(
        String memberName,
        String commentContent,
        LocalDateTime createdAt
) {
}
