package com.airoom.airoom.board.model.dto.comment;

public record CommentRequest (
        Long commentParentNo,
        String commentContent
)
{ }
