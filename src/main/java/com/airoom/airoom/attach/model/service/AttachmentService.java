package com.airoom.airoom.attach.model.service;

import com.airoom.airoom.attach.model.dto.AttachmentDto;
import com.airoom.airoom.attach.model.repository.AttachmentRepository;
import com.airoom.airoom.board.BoardType;
import com.airoom.airoom.board.entity.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public void saveAttachment(AttachmentDto dto) {
        Attachment attachment = Attachment.builder()
                .boardNo(dto.getBoardNo())
                .boardType(dto.getBoardType())
                .originalName(dto.getOriginalName())
                .savedName(dto.getSavedName())
                .s3Key(dto.getS3Key())
                .build();
        attachmentRepository.save(attachment);
    }

    public void deleteByBoard(Long boardNo, BoardType boardType) {
        attachmentRepository.deleteByBoardNoAndBoardType(boardNo, boardType);
    }

    @Transactional(readOnly = true)
    public List<Attachment> findByBoard(Long boardNo, BoardType boardType) {
        return attachmentRepository.findByBoardNoAndBoardType(boardNo, boardType);
    }

}
