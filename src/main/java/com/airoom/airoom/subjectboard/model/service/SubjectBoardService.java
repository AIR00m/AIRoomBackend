package com.airoom.airoom.subjectboard.model.service;

import com.airoom.airoom.attach.model.dto.AttachmentDto;
import com.airoom.airoom.attach.model.service.AttachmentService;
import com.airoom.airoom.board.BoardType;
import com.airoom.airoom.board.entity.SubjectBoard;
import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.repository.MemberRepository;
import com.airoom.airoom.subjectboard.model.dto.SubjectBoardRequest;
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
    private final AttachmentService attachmentService;


    public Long insertSubjectBoard(SubjectBoardRequest request, List<AttachmentDto> files) {
        Member member = memberRepository.findById(request.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Classroom classroom = classroomRepository.findById(request.getClassroomNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클래스룸입니다."));

        SubjectBoard board = SubjectBoard.builder()
                .sbTitle(request.getTitle())
                .sbContent(request.getContent())
                .sbFocusType(request.isFocusType())
                .member(member)
                .classroom(classroom)
                .build();

        SubjectBoard savedBoard = subjectBoardRepository.save(board);

        attachmentService.saveAll(files, savedBoard.getSbNo(), BoardType.SUBJECT);

        return savedBoard.getSbNo();
    }

    public void updateSubjectBoard(Long boardNo, SubjectBoardRequest request) {
        SubjectBoard board = subjectBoardRepository.findById(boardNo)
                .orElseThrow(() -> new NotFoundException("게시글 없음"));

        board.update(request.getTitle(),request.getContent(),request.isFocusType());

        attachmentService.deleteByBoard(boardNo,BoardType.SUBJECT);
        attachmentService.saveAll(request.getAttachments(),boardNo,BoardType.SUBJECT);
    }

    public void deleteSubjectBoard(Long boardNo) {
        subjectBoardRepository.deleteById(boardNo);
        attachmentService.deleteByBoard(boardNo,BoardType.SUBJECT);
    }
}
