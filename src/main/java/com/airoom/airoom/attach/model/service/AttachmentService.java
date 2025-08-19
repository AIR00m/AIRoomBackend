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

    public void saveAll(List<AttachmentDto> attachments, Long boardNo, BoardType boardType) {
        try {
            List<Attachment> entities = attachments.stream()
                    .map(dto -> Attachment.builder()
                            .boardNo(boardNo)
                            .boardType(boardType)
                            .originalName(dto.getOriginalName())
                            .savedName(dto.getSavedName())
                            .s3Key(dto.getS3Key())
                            .build())
                    .toList();

            attachmentRepository.saveAll(entities);
        }catch (Exception e){
            throw new IllegalStateException("첨부파일 저장 중 오류 발생", e);
        }
    }

    public void deleteByBoard(Long boardNo, BoardType boardType){
        attachmentRepository.deleteByBoardNoAndBoardType(boardNo,boardType);
    }

    @Transactional(readOnly = true)
    public List<Attachment> findByBoard(Long boardNo, BoardType boardType){
        return attachmentRepository.findByBoardNoAndBoardType(boardNo,boardType);
    }

}
