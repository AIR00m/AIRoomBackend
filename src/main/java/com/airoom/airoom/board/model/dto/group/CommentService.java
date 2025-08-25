package com.airoom.airoom.board.model.dto.group;

import com.airoom.airoom.board.entity.Comment;
import com.airoom.airoom.board.entity.GroupBoard;
import com.airoom.airoom.board.model.dto.comment.CommentRequest;
import com.airoom.airoom.board.model.dto.comment.CommentResponse;
import com.airoom.airoom.board.model.dto.comment.SaveCommentResponse;
import com.airoom.airoom.board.model.repository.CommentRepository;
import com.airoom.airoom.board.model.repository.GroupBoardRepository;
import com.airoom.airoom.common.token.CustomUserDetails;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class CommentService {
    private final CommentRepository commentRepository;
    private final GroupBoardRepository groupBoardRepository;
    private final MemberRepository memberRepository;

    public List<CommentResponse> getCommentsByGroupBoardNo(Long groupBoardNo) {
        return commentRepository.getCommentsByGroupBoardNo(groupBoardNo);
    }

    public SaveCommentResponse writeCommentByBoardNo (Long groupBoardNo, CustomUserDetails user, CommentRequest request) {

        GroupBoard groupBoard = groupBoardRepository.findById(groupBoardNo)
                .orElseThrow(()->new IllegalArgumentException("해당 하는 그룹 게시글이 없습니다."));

        Member member = memberRepository.findMemberByMemberId(user.getUsername())
                .orElseThrow(()->new IllegalArgumentException("해당 하는 회원 없습니다."));

        Comment comment = Comment.builder()
                .groupBoard(groupBoard)
                .member(member)
                .parentCommentNo(request.commentParentNo())
                .commentContent(request.commentContent())
                .build();

        commentRepository.save(comment);

        return SaveCommentResponse.builder()
                .memberName(member.getMemberName())
                .commentContent(comment.getCommentContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
