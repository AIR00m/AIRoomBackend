package com.airoom.airoom.attach.model.service;

import com.airoom.airoom.attach.model.dto.AttachmentRequest;
import com.airoom.airoom.attach.model.repository.AttachmentRepository;
import com.airoom.airoom.board.entity.Attachment;
import com.airoom.airoom.board.entity.BoardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public void saveAttachment(AttachmentRequest request) {
        Attachment attachment = buildAttachment(request);
        attachmentRepository.save(attachment);
    }

    public void deleteByBoard(Long boardNo, BoardType boardType) {
        attachmentRepository.deleteByBoardNoAndBoardType(boardNo, boardType);
    }

    @Transactional(readOnly = true)
    public List<Attachment> findByBoard(Long boardNo, BoardType boardType) {
        return attachmentRepository.findByBoardNoAndBoardType(boardNo, boardType);
    }


    /*메소드 추출*/
    private Attachment buildAttachment(AttachmentRequest dto) {
        return Attachment.builder()
                .boardNo(dto.getBoardNo())
                .boardType(dto.getBoardType())
                .originalName(dto.getOriginalName())
                .savedName(dto.getSavedName())
                .s3Key(dto.getS3Key())
                .build();
    }

}
