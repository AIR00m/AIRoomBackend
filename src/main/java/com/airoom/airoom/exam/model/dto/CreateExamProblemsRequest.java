package com.airoom.airoom.exam.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateExamProblemsRequest(
        @NotEmpty(message = "최소 1개 이상의 단원 문제 요청이 필요합니다.")
        List<@Valid ExamProblemRequest> examProblemRequestList
) {
}
