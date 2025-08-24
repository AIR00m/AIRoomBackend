package com.airoom.airoom.board.model.dto.group;

import com.airoom.airoom.board.model.dto.comment.CommentResponse;
import com.airoom.airoom.board.model.repository.CommentRepository;
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

    public List<CommentResponse> getCommentsByGroupBoardNo(Long groupBoardNo) {
        return commentRepository.getCommentsByGroupBoardNo(groupBoardNo);
    }
}
