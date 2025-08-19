package com.airoom.airoom.board.model.service;

import com.airoom.airoom.board.model.dto.AssignmentListDto;
import com.airoom.airoom.board.model.dto.AssignmentRequestDto;
import com.airoom.airoom.board.model.dto.AssignmentRequestDto.AssignTargetDto;
import com.airoom.airoom.board.model.dto.AssignmentResponseDto;
import com.airoom.airoom.common.redis.RedisStreamPublisher;
import com.airoom.airoom.common.redis.model.dto.AssignmentCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final RedisStreamPublisher publisher;

    public void createAssignment(AssignmentRequestDto requestDto) {
        AssignmentCreateDto createDto = AssignmentCreateDto.builder()
                .boardType(requestDto.getAssignBoard().getBoardType())
                .assignBoardTitle(requestDto.getAssignBoard().getAssignBoardTitle())
                .classroomNo(requestDto.getAssignBoard().getClassroomNo())
                .boardContent(requestDto.getAssignBoard().getBoardContent())
                .memberNo(requestDto.getAssignBoard().getMemberNo())
                .targetNo(requestDto.getAssignTargets()
                        .stream()
                        .map(AssignTargetDto::getTargetNo)
                        .toList())
                .build();

        publisher.createAssignment(createDto);

        // db 저장 등 로직 수행
    }

    public List<AssignmentListDto> getAllAssignments() {

        return Arrays.asList(
                AssignmentListDto.builder()
                        .id(1L)
                        .title("알파벳 단어 카드 만들기")
                        .isGroupAssignment(false)
                        .startDate("2025-08-01")
                        .dueDate("2025-08-05")
                        .submitStatus("false")
                        .build(),
                AssignmentListDto.builder()
                        .id(2L)
                        .title("수학 연산 게임 대회")
                        .isGroupAssignment(true)
                        .startDate("2025-08-03")
                        .dueDate("2025-08-08")
                        .submitStatus("true")
                        .build(),
                AssignmentListDto.builder()
                        .id(3L)
                        .title("과학 실험 결과 발표")
                        .isGroupAssignment(true)
                        .startDate("2025-08-02")
                        .dueDate("2025-08-12")
                        .submitStatus("false")
                        .build(),
                AssignmentListDto.builder()
                        .id(4L)
                        .title("영어 일기 쓰기")
                        .isGroupAssignment(false)
                        .startDate("2025-08-01")
                        .dueDate("2025-08-15")
                        .submitStatus("false")
                        .build(),
                AssignmentListDto.builder()
                        .id(5L)
                        .title("독서 감상문 올리기")
                        .isGroupAssignment(false)
                        .startDate("2025-08-05")
                        .dueDate("2025-08-20")
                        .submitStatus("true")
                        .build(),
                AssignmentListDto.builder()
                        .id(6L)
                        .title("미술 작품 사진 공유")
                        .isGroupAssignment(true)
                        .startDate("2025-08-10")
                        .dueDate("2025-08-25")
                        .submitStatus("false")
                        .build(),
                AssignmentListDto.builder()
                        .id(7L)
                        .title("세계 지도 보고 나라 찾기")
                        .isGroupAssignment(false)
                        .startDate("2025-08-12")
                        .dueDate("2025-08-18")
                        .submitStatus("false")
                        .build(),
                AssignmentListDto.builder()
                        .id(8L)
                        .title("AI와 대화하는 챗봇 사용 후기 작성")
                        .isGroupAssignment(false)
                        .startDate("2025-08-14")
                        .dueDate("2025-08-28")
                        .submitStatus("true")
                        .build(),
                AssignmentListDto.builder()
                        .id(9L)
                        .title("교과서 문제 풀이 모둠 과제")
                        .isGroupAssignment(true)
                        .startDate("2025-08-15")
                        .dueDate("2025-08-30")
                        .submitStatus("false")
                        .build(),
                AssignmentListDto.builder()
                        .id(10L)
                        .title("자기소개 PPT 작성해 발표하기")
                        .isGroupAssignment(false)
                        .startDate("2025-08-17")
                        .dueDate("2025-08-22")
                        .submitStatus("true")
                        .build()
        );
    }
}
