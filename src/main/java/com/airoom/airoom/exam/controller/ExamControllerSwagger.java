package com.airoom.airoom.exam.controller;

import com.airoom.airoom.common.value.MemberRole;
import com.airoom.airoom.exam.entity.value.ExamStatus;
import com.airoom.airoom.exam.model.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Exam 관련 API", description = "Exam 관련 API")
public interface ExamControllerSwagger {
    @Operation(
            summary = "시험 생성 API",
            description = "새로운 시험을 생성합니다."
    )
    public ResponseEntity<Void> createExam(
            @RequestBody @Valid final CreateExamRequest request
    );


    @Operation(
            summary = "난이도, 단원별 랜덤 출제 문제 리스트 조회 API",
            description = "난이도, 단원별 랜덤 출제 문제 리스트를 조회합니다."
    )
    public CreateExamProblemsResponse getExamProblemsByLevelAndUnit(@RequestBody @Valid final CreateExamProblemsRequest request);

    @Operation(
            summary = "시험 문제 교체 API",
            description = "시험 문제를 교체합니다."
    )
    public ResponseEntity<ExamProblemResponse> replaceExamProblem(@RequestBody @Valid final ReplaceExamProblemRequest request);


    @Operation(
            summary = "시험출제문제 전체조회 API",
            description = "시험출제문제를 전체조회합니다."
    )
    public ExamDetailResponse getExamProblems(@PathVariable Long examNo);

    @Operation(
            summary = "시험문제 풀이 제출 및 채점 API",
            description = "시험문제 풀이를 제출 및 채점합니다."
    )
    public ResponseEntity<SubmitExamProblemsResponse> markAndSubmitExamProblems(@RequestBody @Valid final SubmitExamProblemsRequest request);


    @Operation(
            summary = "진행/완료/전체 시험 조회 API",
            description = "진행/완료/전체 시험을 조회합니다."
    )
    public List<ExamListResponse> getExams(
            @PathVariable final Long classroomMemberNo,
            @RequestParam("examStatus") final ExamStatus examStatus,
            @RequestParam("memberRole") final MemberRole memberRole
    );

}
