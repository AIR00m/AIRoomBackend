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

import java.net.URI;

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
        Long examId;
        try {
            examId = examService.createExam(request);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.created(URI.create("/exam/" + examId)).build();
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
    @GetMapping("/examProblems/{examNo}")
    public void getExamProblems(@PathVariable Long examNo) {

    }

    /**
     * 시험문제 채점
     */
    @Override
    @PostMapping("/check")
    public void markExamProblems() {

    }

    /**
     * 시험 삭제
     */
    @Override
    @DeleteMapping("/{examNo}")
    public void deleteExam(@PathVariable Long examNo) {

    }

    //시험 수정 기능은 제외
}
