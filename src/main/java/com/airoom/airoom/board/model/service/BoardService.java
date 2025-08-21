package com.airoom.airoom.board.model.service;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import com.airoom.airoom.board.model.dto.*;
import com.airoom.airoom.board.model.repository.AssignBoardRepository;
import com.airoom.airoom.board.model.repository.AssignTargetRepository;
import com.airoom.airoom.board.model.repository.HomeworkRepository;
import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.repository.ClassroomGroupRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomTeacherRepository;
import com.airoom.airoom.common.redis.RedisStreamPublisher;
import com.airoom.airoom.common.redis.model.dto.AssignmentCreateDto;
import com.airoom.airoom.member.entity.Member;
import jakarta.persistence.EntityNotFoundException;
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
@Transactional
public class  BoardService {
    private final RedisStreamPublisher publisher;
    private final AssignBoardRepository assignBoardRepository;
    private final ClassroomGroupRepository classroomGroupRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final AssignTargetRepository assignTargetRepository;
    private final HomeworkRepository homeworkRepository;

    public void createAssignment(AssignmentCreateRequest request) {
        //유효성 검사
        validateRequest(request);
        //assignBoard save
        AssignBoard assignBoard = createAndSaveAssignBoard(request.assignBoard());
        //assignTarget save
        List<Long> savedTargetIds  = saveAssignTargets(assignBoard, request.assignTargets()); // 🔧 수정: 변수명 변경
        //redis stream 메세지 발행
//        publishAssignmentCreated(assignBoard, savedTargetIds , request); // 🔧 수정

        log.info("과제 생성 완료 - AssignBoard ID: {}, 대상자 수: {}",
                assignBoard.getAssignBoardNo(), savedTargetIds .size()); // 🔧 수정
    }

    /**
     * Redis Stream 메시지 발행
     */
//    private void publishAssignmentCreated(AssignBoard assignBoard, List<Long> targetClassroomStudentNos, AssignmentCreateRequest request) { // 🔧 수정
//        List<Long> targetMemberNos = targetClassroomStudentNos.stream()
//                .map(classroomStudentNo -> {
//                    ClassroomStudent student = classroomStudentRepository.findById(classroomStudentNo)
//                            .orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다: " + classroomStudentNo));
//                    return student.getStudent().getMemberNo();
//                })
//                .collect(Collectors.toList());
//
//        AssignmentCreateDto createDto = AssignmentCreateDto.builder()
//                .boardType("ASSIGN")
//                .assignBoardTitle(assignBoard.getAssignBoardTitle())
//                .classroomNo(assignBoard.getClassroom().getClassroomNo())
//                .boardContent(assignBoard.getAssignBoardContent())
//                .memberNo(assignBoard.getMember().getMemberNo())
//                .targetNo(targetMemberNos)  // 🔧 수정: memberNo 리스트 사용
//                .build();
//
//        // Redis Stream에 메시지 발행
//        publisher.createAssignment(createDto);
//
//        log.info("Redis Stream 메시지 발행 완료 - 과제 ID: {}, 대상자(memberNo): {}",
//                assignBoard.getAssignBoardNo(), targetMemberNos); // 🔧 수정
//    }

    /**
     * AssignBoard 엔티티 생성 및 저장
     */
    private AssignBoard createAndSaveAssignBoard(AssignmentCreateRequest.AssignBoard boardDto) {

        // 1) ClassroomTeacher로 실제 Member 조회
        ClassroomTeacher classroomTeacher = classroomTeacherRepository.findById(boardDto.classroomTeacherNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 교사 정보를 찾을 수 없습니다: " + boardDto.classroomTeacherNo()));

        // 2) ClassroomTeacher에서 Member 추출
        Member teacher = classroomTeacher.getTeacher();

        AssignBoard assignBoard = AssignBoard.builder()
                .assignBoardContent(boardDto.assignBoardContent())
                .assignBoardTitle(boardDto.assignBoardTitle())
                .assignStart(boardDto.assignStart())
                .assignEnd(boardDto.assignEnd())
                .member(teacher)
                .classroom(Classroom.builder().classroomNo(boardDto.classroomNo()).build())
                .build();

        return assignBoardRepository.save(assignBoard);
    }

    /**
     * AssignTarget 저장 - 개별과제/모둠과제 분기 처리
     * @return 실제 과제를 받을 모든 학생들의 classroomStudentNo 리스트
     */
    private List<Long> saveAssignTargets(AssignBoard assignBoard, List<AssignmentCreateRequest.AssignTarget> assignTargets) {
        List<Long> allTargetIds = new ArrayList<>();
        for (AssignmentCreateRequest.AssignTarget targetDto : assignTargets) {
            boolean isGroup = Boolean.TRUE.equals(targetDto.groupAssignType());
            Long targetNo = targetDto.targetNo();

            if (isGroup) {
                saveGroupAssignTarget(assignBoard, targetNo); // groupAssignType=true 고정
                log.debug("모둠 과제 저장 - assignBoardNo={}, groupNo={}", assignBoard.getAssignBoardNo(), targetNo);
            } else {
                saveIndividualAssignTarget(assignBoard, targetNo); // groupAssignType=false 고정
                log.debug("개별 과제 저장 - assignBoardNo={}, classroomStudentNo={}", assignBoard.getAssignBoardNo(), targetNo);
            }
            allTargetIds.add(targetNo);
        }
        return allTargetIds;
    }


    /**
     * 모둠 과제 AssignTarget 저장
     * @return 해당 모둠 구성원들의 classroomStudentNo 리스트
     */
    private void saveGroupAssignTarget(AssignBoard assignBoard, Long groupNo) {
        AssignTarget assignTarget = AssignTarget.builder()
                .targetNo(groupNo)               // groupNo 저장
                .groupAssignType(true)           // 모둠 과제
                .assignBoard(assignBoard)
                .build();
        assignTargetRepository.save(assignTarget);
    }


    /**
     * 개별 AssignTarget 저장
     * @param classroomStudentNo 클래스룸 학생 번호 (ClassroomStudent PK)
     */
    private void saveIndividualAssignTarget(AssignBoard assignBoard, Long classroomStudentNo) {
        AssignTarget assignTarget = AssignTarget.builder()
                .targetNo(classroomStudentNo)    // classroomStudentNo 저장
                .groupAssignType(false)          // 개별 과제
                .assignBoard(assignBoard)
                .build();
        assignTargetRepository.save(assignTarget);
    }

    /**
     * 유효성 검증
     */
    private void validateRequest(AssignmentCreateRequest request) {
        if (request.assignTargets() == null || request.assignTargets().isEmpty()) {
            throw new IllegalArgumentException("과제 대상자를 선택해주세요.");
        }
        if (request.assignBoard().assignStart().isAfter(request.assignBoard().assignEnd())) {
            throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.");
        }

        boolean hasGroupTarget = request.assignTargets().stream()
                .anyMatch(t -> Boolean.TRUE.equals(t.groupAssignType()));
        boolean hasIndividualTarget = request.assignTargets().stream()
                .anyMatch(t -> !Boolean.TRUE.equals(t.groupAssignType()));

        if (hasGroupTarget && hasIndividualTarget) {
            throw new IllegalArgumentException("모둠 과제와 개별 과제를 함께 선택할 수 없습니다.");
        }

        // 동일 타입 일관성 검증(선택)
        boolean mode = Boolean.TRUE.equals(request.assignTargets().get(0).groupAssignType());
        boolean allSame = request.assignTargets().stream()
                .allMatch(t -> Boolean.TRUE.equals(t.groupAssignType()) == mode);
        if (!allSame) {
            throw new IllegalArgumentException("대상 타입이 일관되지 않습니다.");
        }

        // 존재성 검증(선택)
        if (mode) {
            for (AssignmentCreateRequest.AssignTarget t : request.assignTargets()) {
                if (!classroomGroupRepository.existsById(t.targetNo())) {
                    throw new IllegalArgumentException("존재하지 않는 모둠입니다. groupNo=" + t.targetNo());
                }
            }
        } else {
            for (AssignmentCreateRequest.AssignTarget t : request.assignTargets()) {
                if (!classroomStudentRepository.existsById(t.targetNo())) {
                    throw new IllegalArgumentException("존재하지 않는 학생입니다. classroomStudentNo=" + t.targetNo());
                }
            }
        }
    }

    /**
     * 학생용 과제 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AssignmentListResponseDto> getAssignmentsForStudent(Long classroomNo, Long classroomStudentNo) {
        // 1) 해당 학생의 개별 AssignTarget 조회
        List<AssignTarget> individualTargets = assignTargetRepository
                .findByAssignBoard_Classroom_ClassroomNoAndTargetNoAndGroupAssignTypeFalse(
                        classroomNo, classroomStudentNo);

        // 2) 해당 학생이 속한 그룹의 모둠 AssignTarget 조회
        ClassroomStudent student = classroomStudentRepository.findById(classroomStudentNo)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        List<AssignTarget> groupTargets = new ArrayList<>();
        if (student.getClassroomGroup() != null) {
            groupTargets = assignTargetRepository
                    .findByAssignBoard_Classroom_ClassroomNoAndTargetNoAndGroupAssignTypeTrue(
                            classroomNo, student.getClassroomGroup().getGroupNo());
        }

        // 3) 개별과 모둠 AssignTarget을 합쳐서 DTO 생성
        List<AssignTarget> allTargets = new ArrayList<>();
        allTargets.addAll(individualTargets);
        allTargets.addAll(groupTargets);

        return allTargets.stream()
                .map(assignTarget -> {
                    AssignBoard assignBoard = assignTarget.getAssignBoard();

                    // 제출 여부 체크 (AssignTarget 기준)
                    boolean isSubmitted = homeworkRepository.existsByAssignTarget(assignTarget);

                    return new AssignmentListResponseDto(
                            assignBoard.getAssignBoardNo(),
                            assignBoard.getAssignBoardTitle(),
                            assignTarget.isGroupAssignType(), // AssignTarget의 실제 값 사용
                            assignBoard.getAssignStart().toLocalDate().toString(),
                            assignBoard.getAssignEnd().toLocalDate().toString(),
                            isSubmitted ? "true" : "false"
                    );
                })
                .collect(Collectors.toList());
    }

//    @Transactional(readOnly = true)
//    public List<AssignmentListResponseDto> getAssignmentsForStudent(Long classroomNo, Long classroomStudentNo) {
//        List<AssignBoard> assignBoards = assignBoardRepository.findAssignmentsForStudent(classroomNo, classroomStudentNo);
//
//        return assignBoards.stream()
//                .map(assignBoard -> {
//                    // classroomStudentNo를 targetNo로 하는 AssignTarget에 대한 Homework 존재 여부 확인
//                    boolean isSubmitted = homeworkRepository.existsByAssignTarget_AssignBoardAndAssignTarget_TargetNo(
//                            assignBoard, classroomStudentNo
//                    );
//
//                    return new AssignmentListResponseDto(
//                            assignBoard.getAssignBoardNo(),
//                            assignBoard.getAssignBoardTitle(),
//                            checkIfGroupAssignment(assignBoard),
//                            assignBoard.getAssignStart().toLocalDate().toString(),
//                            assignBoard.getAssignEnd().toLocalDate().toString(),
//                            isSubmitted ? "true" : "false"
//                    );
//                })
//                .collect(Collectors.toList());
//    }


    /**
     * 선생님용 과제 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AssignmentListResponseDto> getAssignmentsForTeacher(Long classroomNo) {
        // 해당 클래스룸의 모든 과제 조회
        List<AssignBoard> assignBoards = assignBoardRepository.findByClassroomClassroomNo(classroomNo);

        return assignBoards.stream()
                .map(assignBoard -> new AssignmentListResponseDto( // record 생성자 사용
                        assignBoard.getAssignBoardNo(),
                        assignBoard.getAssignBoardTitle(),
                        checkIfGroupAssignment(assignBoard), // 모둠/개별 과제 구분
                        assignBoard.getAssignStart().toLocalDate().toString(),
                        assignBoard.getAssignEnd().toLocalDate().toString(),
                        "N/A" // 선생님용에서는 submitStatus가 의미 없음
                ))
                .collect(Collectors.toList());
    }

    /**
     * 모둠 과제인지 확인
     */
    private boolean checkIfGroupAssignment(AssignBoard assignBoard) {
        return assignTargetRepository.existsByAssignBoardAndGroupAssignTypeTrue(assignBoard);
    }

    /**
     * 과제 상세 조회
     */
//    public AssignmentDetailResponseDto getAssignmentByAssignBoardNo(Long assignBoardNo) {
//        // 1. AssignBoard 조회
//        AssignBoard assignBoard = assignBoardRepository.findById(assignBoardNo)
//                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
//
//        // 2. 연관 데이터 조회 및 DTO 변환
//        return  AssignmentDetailResponseDto.builder()
//                .assignBoardNo(assignBoard.getAssignBoardNo())
//                .title(assignBoard.getAssignBoardTitle())
//                .content(assignBoard.getAssignBoardContent())
//                .startDate(assignBoard.getAssignStart().toString())
//                .dueDate(assignBoard.getAssignEnd().toString())
//                .isGroupAssignment(checkIfGroupAssignment(assignBoard))
//                .targetStudents(getTargetStudents(assignBoard))
//                .assignedGroups(getAssignedGroups(assignBoard))
//                .teacherAttachments(getTeacherAttachments(assignBoard))
//                .mySubmission(getMySubmission(assignBoard, currentMemberNo))
//                .build();
//
//    }

    public void submitAssignment(Long assignmentId, AssignmentSubmissionRequestDto submissionDto) {
        log.info("과제 제출 처리 - 과제 ID: {}, 내용: {}", assignmentId, submissionDto.getContent());
        log.info("첨부파일 개수: {}", submissionDto.getFiles() != null ? submissionDto.getFiles().size() : 0);

        // 실제로는 DB에 저장하는 로직 구현
        // 여기서는 로그만 출력
    }
}
