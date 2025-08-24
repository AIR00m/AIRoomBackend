package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.Comment;
import com.airoom.airoom.board.model.dto.comment.CommentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
            SELECT NEW com.airoom.airoom.board.model.dto.comment.CommentResponse(
                        c.commentNo,
                        m.memberName,
                        c.commentContent,
                        c.createdAt,
                        c.parentCommentNo
                        )
            FROM Comment c
            JOIN c.member m
            WHERE c.groupBoard.groupBoardNo = :groupBoardNo
            """
    )
    List<CommentResponse> getCommentsByGroupBoardNo (Long groupBoardNo);
}
