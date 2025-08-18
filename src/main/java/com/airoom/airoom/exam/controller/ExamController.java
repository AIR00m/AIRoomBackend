package com.airoom.airoom.exam.controller;

import com.airoom.airoom.exam.model.dto.CreateExamProblemsRequest;
import com.airoom.airoom.exam.model.dto.CreateExamProblemsResponse;
import com.airoom.airoom.exam.model.dto.CreateExamRequest;
import com.airoom.airoom.exam.model.dto.ReplaceExamProblemRequest;
import com.airoom.airoom.exam.model.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exam")
public class ExamController implements ExamControllerSwagger {
    private final ExamService examService;

    /**
     * 시험 생성
     */
    @PostMapping
    public ResponseEntity<Long> createExam(
            @Valid @RequestBody final CreateExamRequest request
    ) {
        return null;
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
    public void replaceExamProblem(
            @Valid @RequestBody final ReplaceExamProblemRequest request
    ) {

    }

    /**
     * 미완료 시험 조회
     * 멤버 타입별로 교사, 학생별 데이터가 다름
     */
    @GetMapping("/incomplete/{classroomNo}")
    public void getInCompleteExams(
            @PathVariable final Integer classroomNo
    ) {

    }

    /**
     * 완료 시험 조회
     * 멤버 타입별로 교사, 학생별 데이터가 다름
     */
    @GetMapping("/complete/{classroomNo}")
    public void getCompleteExams(
            @PathVariable final Integer classroomNo
    ) {

    }

    /**
     * 전체 시험 조회
     * 멤버 타입별로 교사, 학생별 데이터가 다름
     */
    @GetMapping("/all/{classroomNo}")
    public void getExamsAll(
            @PathVariable final Integer classroomNo
    ) {

    }

    /**
     * 시험별 시험문제 조회
     */
    @Override
    public void getExamProblems() {

    }

    /**
     * 시험문제 채점
     */
    @Override
    public void markExamProblems() {

    }

    /**
     * 시험 삭제
     */
    @Override
    public void deleteExam() {

    }

    //시험 수정 기능은 제외
}
