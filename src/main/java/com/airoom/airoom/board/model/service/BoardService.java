package com.airoom.airoom.board.model.service;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import com.airoom.airoom.board.model.dto.*;
import com.airoom.airoom.board.model.repository.AssignBoardRepository;
import com.airoom.airoom.board.model.repository.AssignTargetRepository;
import com.airoom.airoom.board.model.repository.HomeworkRepository;
import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.model.repository.ClassroomGroupRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.common.redis.RedisStreamPublisher;
import com.airoom.airoom.common.redis.model.dto.AssignmentCreateDto;
import com.airoom.airoom.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
    private final RedisStreamPublisher publisher;
    private final AssignBoardRepository assignBoardRepository;
    private final ClassroomGroupRepository classroomGroupRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final AssignTargetRepository assignTargetRepository;
    private final HomeworkRepository homeworkRepository;

    public void createAssignment(AssignmentCreateRequest request) {
        //유효성 검사
        validateRequest(request);
        //assignBoard save
        AssignBoard assignBoard = createAndSaveAssignBoard(request.assignBoard());
        //assignTarget save
        List<Long> allTargetClassroomStudentNos = saveAssignTargets(assignBoard, request.assignTargets()); // 🔧 수정: 변수명 변경
        //redis stream 메세지 발행
        publishAssignmentCreated(assignBoard, allTargetClassroomStudentNos, request); // 🔧 수정

        log.info("과제 생성 완료 - AssignBoard ID: {}, 대상자 수: {}",
                assignBoard.getAssignBoardNo(), allTargetClassroomStudentNos.size()); // 🔧 수정
    }

    /**
     * Redis Stream 메시지 발행
     */
    private void publishAssignmentCreated(AssignBoard assignBoard, List<Long> targetClassroomStudentNos, AssignmentCreateRequest request) { // 🔧 수정

        // 🔧 추가: classroomStudentNo를 memberNo로 변환
        List<Long> targetMemberNos = targetClassroomStudentNos.stream()
                .map(classroomStudentNo -> {
                    ClassroomStudent student = classroomStudentRepository.findById(classroomStudentNo)
                            .orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다: " + classroomStudentNo));
                    return student.getStudent().getMemberNo();
                })
                .collect(Collectors.toList());

        AssignmentCreateDto createDto = AssignmentCreateDto.builder()
                .boardType("ASSIGN")
                .assignBoardTitle(assignBoard.getAssignBoardTitle())
                .classroomNo(assignBoard.getClassroom().getClassroomNo())
                .boardContent(assignBoard.getAssignBoardContent())
                .memberNo(assignBoard.getMember().getMemberNo())
                .targetNo(targetMemberNos)  // 🔧 수정: memberNo 리스트 사용
                .build();

        // Redis Stream에 메시지 발행
        publisher.createAssignment(createDto);

        log.info("Redis Stream 메시지 발행 완료 - 과제 ID: {}, 대상자(memberNo): {}",
                assignBoard.getAssignBoardNo(), targetMemberNos); // 🔧 수정
    }

    /**
     * AssignBoard 엔티티 생성 및 저장
     */
    private AssignBoard createAndSaveAssignBoard(AssignmentCreateRequest.AssignBoard boardDto) {

        AssignBoard assignBoard = AssignBoard.builder()
                .assignBoardContent(boardDto.assignBoardContent())
                .assignBoardTitle(boardDto.assignBoardTitle())
                .assignStart(boardDto.assignStart())
                .assignEnd(boardDto.assignEnd())
                .member(Member.builder().memberNo(boardDto.memberNo()).build())
                .classroom(Classroom.builder().classroomNo(boardDto.classroomNo()).build())
                .build();

        return assignBoardRepository.save(assignBoard);
    }

    /**
     * AssignTarget 저장 - 개별과제/모둠과제 분기 처리
     * @return 실제 과제를 받을 모든 학생들의 classroomStudentNo 리스트
     */
    private List<Long> saveAssignTargets(AssignBoard assignBoard, List<AssignmentCreateRequest.AssignTarget> assignTargets) {

        List<Long> allTargetClassroomStudentNos = new ArrayList<>(); // 🔧 수정: 변수명 변경

        for (AssignmentCreateRequest.AssignTarget targetDto : assignTargets) {

            if (targetDto.groupAssignType()) {
                // 🧑‍🤝‍🧑 모둠 과제: 모둠의 모든 구성원에게 개별 AssignTarget 생성
                List<Long> groupClassroomStudentNos = saveGroupAssignTargets(assignBoard, targetDto.targetNo()); // 🔧 수정
                allTargetClassroomStudentNos.addAll(groupClassroomStudentNos); // 🔧 수정

            } else {
                // 👤 개별 과제: 해당 학생에게 직접 AssignTarget 생성
                saveIndividualAssignTarget(assignBoard, targetDto.targetNo(), false);
                allTargetClassroomStudentNos.add(targetDto.targetNo()); // 🔧 수정: classroomStudentNo 추가
            }
        }

        return allTargetClassroomStudentNos; // 🔧 수정
    }

    /**
     * 모둠 과제 AssignTarget 저장
     * @return 해당 모둠 구성원들의 classroomStudentNo 리스트
     */
    private List<Long> saveGroupAssignTargets(AssignBoard assignBoard, Long groupNo) {

        // 해당 모둠의 모든 구성원 조회
        List<ClassroomStudent> groupMembers = classroomStudentRepository.findClassroomStudentsByGroupNo(groupNo);

        log.info("모둠 {} 구성원 {}명에게 과제 할당", groupNo, groupMembers.size());

        List<Long> classroomStudentNos = new ArrayList<>(); // 🔧 수정: 변수명 변경

        // 각 구성원에게 AssignTarget 생성 (groupAssignType = true)
        for (ClassroomStudent member : groupMembers) {
            // 🔧 수정: classroomStudentNo로 저장
            saveIndividualAssignTarget(assignBoard, member.getClassRoomStudentNo(), true);
            classroomStudentNos.add(member.getClassRoomStudentNo()); // 🔧 수정
        }

        return classroomStudentNos; // 🔧 수정
    }

    /**
     * 개별 AssignTarget 저장
     * @param classroomStudentNo 클래스룸 학생 번호 (ClassroomStudent PK)
     */
    private void saveIndividualAssignTarget(AssignBoard assignBoard, Long classroomStudentNo, boolean isGroupAssign) { // 🔧 수정: 매개변수명 변경

        AssignTarget assignTarget = AssignTarget.builder()
                .targetNo(classroomStudentNo)             // 🔧 수정: classroomStudentNo 저장
                .groupAssignType(isGroupAssign)           // 모둠과제 여부
                .assignBoard(assignBoard)                 // 연관 엔티티
                .build();

        assignTargetRepository.save(assignTarget);

        log.debug("AssignTarget 저장 완료 - classroomStudentNo: {}, 모둠과제: {}", classroomStudentNo, isGroupAssign); // 🔧 수정: 로그 메시지 변경
    }

    /**
     * 유효성 검증
     */
    private void validateRequest(AssignmentCreateRequest request) {
        if (request.assignTargets().isEmpty()) {
            throw new IllegalArgumentException("과제 대상자를 선택해주세요.");
        }

        if (request.assignBoard().assignStart().isAfter(request.assignBoard().assignEnd())) {
            throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.");
        }

        // 모둠 과제인데 개별 학생이 선택되었거나, 개별 과제인데 모둠이 선택된 경우 검증
        boolean hasGroupTarget = request.assignTargets().stream().anyMatch(AssignmentCreateRequest.AssignTarget::groupAssignType);
        boolean hasIndividualTarget = request.assignTargets().stream().anyMatch(target -> !target.groupAssignType());

        if (hasGroupTarget && hasIndividualTarget) {
            throw new IllegalArgumentException("모둠 과제와 개별 과제를 함께 선택할 수 없습니다.");
        }
    }



    /**
     * 학생용 과제 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AssignmentListResponseDto> getAssignmentsForStudent(Long classroomNo, Long studentNo) {
        List<AssignBoard> assignBoards = assignBoardRepository.findAssignmentsForStudent(classroomNo, studentNo);

        return assignBoards.stream()
                .map(assignBoard -> {
                    // ✅ 수정된 메서드 호출
                    boolean isSubmitted = homeworkRepository.existsByAssignBoardAndStudentMemberNo(
                            assignBoard, studentNo);

                    return AssignmentListResponseDto.builder()
                            .id(assignBoard.getAssignBoardNo())
                            .title(assignBoard.getAssignBoardTitle())
                            .isGroupAssignment(checkIfGroupAssignment(assignBoard))
                            .startDate(assignBoard.getAssignStart().toLocalDate().toString())
                            .dueDate(assignBoard.getAssignEnd().toLocalDate().toString())
                            .submitStatus(isSubmitted ? "true" : "false")
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 선생님용 과제 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AssignmentListResponseDto> getAssignmentsForTeacher(Long classroomNo) {
        List<AssignBoard> assignBoards = assignBoardRepository.findByClassroomClassroomNo(classroomNo);

        return assignBoards.stream()
                .map(assignBoard -> AssignmentListResponseDto.builder()
                        .id(assignBoard.getAssignBoardNo())
                        .title(assignBoard.getAssignBoardTitle())
                        .isGroupAssignment(checkIfGroupAssignment(assignBoard))
                        .startDate(assignBoard.getAssignStart().toLocalDate().toString())
                        .dueDate(assignBoard.getAssignEnd().toLocalDate().toString())
                        .submitStatus("true") // 선생님용에서는 의미없음
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 모둠 과제인지 확인
     */
    private boolean checkIfGroupAssignment(AssignBoard assignBoard) {
        return assignTargetRepository.existsByAssignBoardAndGroupAssignTypeTrue(assignBoard);
    }


    public void submitAssignment(Long assignmentId, AssignmentSubmissionRequestDto submissionDto) {
        log.info("과제 제출 처리 - 과제 ID: {}, 내용: {}", assignmentId, submissionDto.getContent());
        log.info("첨부파일 개수: {}", submissionDto.getFiles() != null ? submissionDto.getFiles().size() : 0);

        // 실제로는 DB에 저장하는 로직 구현
        // 여기서는 로그만 출력
    }
}
