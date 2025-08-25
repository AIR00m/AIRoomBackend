package com.airoom.airoom.exam.controller;

import com.airoom.airoom.common.value.MemberRole;
import com.airoom.airoom.exam.entity.value.ExamStatus;
import com.airoom.airoom.exam.model.dto.*;
import com.airoom.airoom.exam.model.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/exam")
public class ExamController implements ExamControllerSwagger {
    private final ExamService examService;

    /**
     * 시험 생성
     */
    @PostMapping
    public ResponseEntity<Void> createExam(
            @Valid @RequestBody final CreateExamRequest request
    ) {
        return ResponseEntity.created(URI.create("/examProblems/" + examService.createExam(request))).build();
    }

    /**
     * 난이도, 단원별 랜덤 문제 출제
     */
    @PostMapping("/level-unit/problems")
    public CreateExamProblemsResponse getExamProblemsByLevelAndUnit(
            @Valid @RequestBody final CreateExamProblemsRequest request
    ) {
        return examService.getExamProblemsByLevelAndUnit(request);
    }

    /**
     * 시험문제 교체
     */
    @PostMapping("/problem")
    public ResponseEntity<ExamProblemResponse> replaceExamProblem(
            @Valid @RequestBody final ReplaceExamProblemRequest request
    ) {
        return ResponseEntity.ok(examService.replaceExamProblem(request));
    }

    /**
     * 시험출제문제 전체조회 = 시험 상세조회
     */
    @Override
    @GetMapping("/exam-problems/{examNo}")
    public ExamDetailResponse getExamProblems(@PathVariable final Long examNo) {
        return examService.getExamProblems(examNo);
    }

    /**
     * 시험문제 채점 & 제출
     */
    @Override
    @PostMapping("/submit")
    public ResponseEntity<SubmitExamProblemsResponse> markAndSubmitExamProblems(@RequestBody @Valid final SubmitExamProblemsRequest request) {
        return ResponseEntity.ok(examService.markAndSubmitExamProblems(request));
    }

    /**
     * 진행/완료/전체 시험 조회
     * 멤버 타입별로 교사, 학생별 데이터가 다름
     */
    @GetMapping("/{classroomMemberNo}")
    public List<ExamListResponse> getExams(
            @PathVariable final Long classroomMemberNo,
            @RequestParam("examStatus") final ExamStatus examStatus,
            @RequestParam("memberRole") final MemberRole memberRole
    ) {
        return examService.getExams(classroomMemberNo, examStatus, memberRole);
    }


    /**
     * 시험별 학생의 정답 리스트 조회
     */
    @Override
    @GetMapping("/answer/student/{classroomStudentNo}/{examNo}")
    public List<StudentAnswerResponse> getStudentAnswersByClassroomStudent(
            @PathVariable final Long classroomStudentNo,
            @PathVariable final Long examNo
    ) {
        return examService.getStudentAnswersByClassroomStudent(classroomStudentNo, examNo);
    }


    /**
     * 시험별 학급의 정답 리스트 조회
     */
    @GetMapping("/answer/classroom/{classroomNo}/{examNo}")
    @Override
    public List<StudentAnswerByClassroomResponse> getStudentAnswersByClassroom(
            @PathVariable final Long classroomNo,
            @PathVariable final Long examNo
    ) {
        return examService.getStudentAnswersByClassroom(classroomNo, examNo);
    }

    //시험 수정,삭제 기능은 제외
}
