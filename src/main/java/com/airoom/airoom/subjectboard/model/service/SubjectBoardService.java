package com.airoom.airoom.subjectboard.model.service;

import com.airoom.airoom.attach.model.dto.AttachmentResponse;
import com.airoom.airoom.attach.model.repository.AttachmentRepository;
import com.airoom.airoom.attach.model.service.AttachmentService;
import com.airoom.airoom.attach.model.service.PresignedUrlService;
import com.airoom.airoom.board.entity.Attachment;
import com.airoom.airoom.board.entity.BoardType;
import com.airoom.airoom.board.entity.SubjectBoard;
import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomTeacherRepository;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.repository.MemberRepository;
import com.airoom.airoom.subjectboard.model.dto.SubjectBoardListResponse;
import com.airoom.airoom.subjectboard.model.dto.SubjectBoardRequest;
import com.airoom.airoom.subjectboard.model.dto.SubjectBoardViewResponse;
import com.airoom.airoom.subjectboard.model.repository.SubjectBoardRepository;
import com.amazonaws.services.kms.model.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectBoardService {

    private final SubjectBoardRepository subjectBoardRepository;
    private final MemberRepository memberRepository;
    private final ClassroomRepository classroomRepository;
    private final AttachmentRepository attachmentRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;

    private final PresignedUrlService presignedUrlService;
    private final AttachmentService attachmentService;

    @Transactional(readOnly = true)
    public List<SubjectBoardListResponse> getAllSubjectBoards(Long classNo) {
        Classroom classroom = classroomRepository.findById(classNo)
                .orElseThrow(() -> new NotFoundException("해당 클래스룸이 존재하지 않습니다"));

        return subjectBoardRepository.findByClassroom(classroom)
                .stream().map(this::buildSubjectBoardListResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubjectBoardViewResponse getSubjectBoard(Long boardNo) {
        SubjectBoard board = subjectBoardRepository.findById(boardNo)
                .orElseThrow(() -> new NotFoundException("해당 게시글이 존재하지 않습니다"));

        List<AttachmentResponse> attachments = attachmentRepository.findByBoardNoAndBoardType(boardNo, BoardType.SUBJECT).stream()
                .map(this::buildAttachmentDto).toList();

        return buildSubjectBoardViewResponse(boardNo, board, attachments);
    }

    public Long insertSubjectBoard(SubjectBoardRequest request) {
        ClassroomTeacher classroomTeacher = classroomTeacherRepository.findById(request.getClassroomTeacherNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Member member = memberRepository.findById(classroomTeacher.getTeacher().getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Classroom classroom = classroomRepository.findById(request.getClassroomNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클래스룸입니다."));

        SubjectBoard board = buildSubjectBoard(request, member, classroom);

        SubjectBoard savedBoard = subjectBoardRepository.save(board);

        return savedBoard.getSbNo();
    }

    public void updateSubjectBoard(Long boardNo, SubjectBoardRequest request) {
        SubjectBoard board = subjectBoardRepository.findById(boardNo)
                .orElseThrow(() -> new NotFoundException("게시글 없음"));

        // 1. 게시글 내용 수정
        board.updateSb(request.getTitle(), request.getContent(), request.isFocusType());

        // 2. 삭제 요청된 첨부파일 제거 (S3 + DB)
        if (request.getDeleteAttachments() != null) {
            for (Long attachNo : request.getDeleteAttachments()) {
                presignedUrlService.deleteAttachment(attachNo);
            }
        }
    }

    public void deleteSubjectBoard(Long boardNo) {
        subjectBoardRepository.deleteById(boardNo);
        attachmentService.deleteByBoard(boardNo, BoardType.SUBJECT);
    }


    /*메소드추출*/
    private AttachmentResponse buildAttachmentDto(Attachment attachment) {
        return AttachmentResponse.builder()
                .attachNo(attachment.getAttachNo())
                .boardNo(attachment.getBoardNo())
                .boardType(attachment.getBoardType())
                .originalName(attachment.getOriginalName())
                .savedName(attachment.getSavedName())
                .s3Key(attachment.getS3Key())
                .build();
    }

    private SubjectBoard buildSubjectBoard(SubjectBoardRequest request, Member member, Classroom classroom) {
        return SubjectBoard.builder()
                .sbTitle(request.getTitle())
                .sbContent(request.getContent())
                .sbFocusType(request.isFocusType())
                .member(member)
                .classroom(classroom)
                .build();
    }

    private SubjectBoardViewResponse buildSubjectBoardViewResponse(Long boardNo, SubjectBoard board, List<AttachmentResponse> attachments) {
        return SubjectBoardViewResponse.builder()
                .sbNo(boardNo)
                .sbTitle(board.getSbTitle())
                .sbContent(board.getSbContent())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .writerName(board.getMember().getMemberName())
                .attachments(attachments)
                .isPinned(board.isSbFocusType())
                .build();
    }

    private SubjectBoardListResponse buildSubjectBoardListResponse(SubjectBoard b) {
        boolean hasAttachment = attachmentRepository.existsByBoardNoAndBoardType(b.getSbNo(), BoardType.SUBJECT);
        return SubjectBoardListResponse.builder()
                .sbNo(b.getSbNo())
                .sbTitle(b.getSbTitle())
                .hasAttachment(hasAttachment)
                .writerName(b.getMember().getMemberName())
                .isPinned(b.isSbFocusType())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
