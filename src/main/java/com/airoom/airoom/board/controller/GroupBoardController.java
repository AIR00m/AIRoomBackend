package com.airoom.airoom.board.controller;

import com.airoom.airoom.board.entity.Attachment;
import com.airoom.airoom.board.model.dto.comment.CommentResponse;
import com.airoom.airoom.board.model.dto.group.CommentService;
import com.airoom.airoom.board.model.dto.group.GroupBoardRequest;
import com.airoom.airoom.board.model.dto.group.GroupBoardResponse;
import com.airoom.airoom.board.model.dto.group.GroupBoardsResponse;
import com.airoom.airoom.board.model.service.GroupBoardService;
import com.airoom.airoom.common.token.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@Slf4j
public class GroupBoardController {

    private final GroupBoardService groupBoardService;
    private final CommentService commentService;

    // 해당하는 모둠 전체 게시글 조회
    @GetMapping("/{groupNo}/boardList")
    public ResponseEntity<List<GroupBoardsResponse>> getBoardListByGroupNo(@PathVariable("groupNo") Long groupNo) {
        return ResponseEntity.ok(groupBoardService.getGroupBoardByGroupNo(groupNo));
    }

    // 모둠 게시판 게시글 작성
    @PostMapping("/{groupNo}/board")
    public ResponseEntity<?> saveBoard(@PathVariable("groupNo") Long groupNo,
                                       @RequestBody GroupBoardRequest request,
                                       @AuthenticationPrincipal CustomUserDetails user) {

        String userId = user.getUsername(); // JWT subject
        Long classroomNo = user.getClassroomNo(); // JWT claims 안의 classroomNo

        groupBoardService.saveGroupBoard(groupNo, classroomNo, userId, request);
        return ResponseEntity.ok().build();
    }

    // 게시판 정보 출력
    @GetMapping("/board/{groupBoardNo}")
    public ResponseEntity<List<CommentResponse>> getGroupBoardByGroupBoardNo(
            @PathVariable Long groupBoardNo) {
        GroupBoardResponse response = groupBoardService.getGroupBoardByGroupNoAndGroupBoardNo(groupBoardNo);
        List<CommentResponse> commentResponse = commentService.getCommentsByGroupBoardNo(groupBoardNo);
        return ResponseEntity.ok(commentResponse);
    }

    // 댓글 작성


}
