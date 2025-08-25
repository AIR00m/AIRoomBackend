package com.airoom.airoom.board.model.dto.group;

import com.airoom.airoom.board.model.dto.comment.CommentResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record TotalResponse (
        GroupBoardResponse groupBoardResponse,
        List<CommentResponse> commentResponse
){
}
