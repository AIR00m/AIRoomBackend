package com.airoom.airoom.board.model.dto.comment;

import java.time.LocalDateTime;

public record CommentResponse (
        Long commentNo,
        String memberName,
        String commentContent,
        LocalDateTime createdAt,
        Long parentCommentNo
)
{ }
