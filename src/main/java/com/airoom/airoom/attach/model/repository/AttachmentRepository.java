package com.airoom.airoom.attach.model.repository;

import com.airoom.airoom.board.entity.Attachment;
import com.airoom.airoom.board.entity.BoardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment,Long> {
    void deleteByBoardNoAndBoardType(Long boardNo, BoardType boardType);

    List<Attachment> findByBoardNoAndBoardType(Long boardNo, BoardType boardType);

    boolean existsByBoardNoAndBoardType(Long boardNo, BoardType boardType);
}
