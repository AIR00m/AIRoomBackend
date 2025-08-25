package com.airoom.airoom.board.controller;

import com.airoom.airoom.board.model.dto.comment.CommentRequest;
import com.airoom.airoom.board.model.dto.comment.SaveCommentResponse;
import com.airoom.airoom.board.model.dto.group.GroupBoardRequest;
import com.airoom.airoom.board.model.dto.group.GroupBoardsResponse;
import com.airoom.airoom.board.model.dto.group.TotalResponse;
import com.airoom.airoom.common.token.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "GroupBoard (모둠 게시판) 관련 API", description = "GroupBoard 관련 API")
public interface GroupBoardControllerSwagger {
    @Operation(
            summary = "해당하는 모둠 전체 게시글 조회",
            description = "매개변수로 그룹번호를 받아서 그거에 해당하는 전체글을 조회"
    )
    public ResponseEntity<List<GroupBoardsResponse>> getBoardListByGroupNo(Long groupNo);
    @Operation(
            summary = "모둠 게시판 게시글 작성",
            description = "매개변수로 그룹번호를 받고 토큰에 있는 UserId 사용"
    )
    public ResponseEntity<Void> saveBoard(@PathVariable("groupNo") Long groupNo,
                                         @RequestBody GroupBoardRequest request,
                                         @AuthenticationPrincipal CustomUserDetails user);
    @Operation(
            summary = "모둠 게시판 댓글 작성",
            description = "매개변수로 그룹번호를 받고 토큰에 있는 UserId 사용"
    )
    public ResponseEntity<SaveCommentResponse> writeCommentByBoardNo(@PathVariable Long groupBoardNo,
                                                                     @RequestBody CommentRequest request,
                                                                     @AuthenticationPrincipal CustomUserDetails user
    );
    @Operation(
            summary = "게시판 정보 출력",
            description = "게시판과 그 게시판에 달린 댓글 출력"
    )
    public ResponseEntity<TotalResponse> getGroupBoardByGroupBoardNo(
            @PathVariable Long groupBoardNo);
}
