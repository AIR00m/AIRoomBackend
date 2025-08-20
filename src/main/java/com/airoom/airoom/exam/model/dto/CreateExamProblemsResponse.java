package com.airoom.airoom.exam.model.dto;

import java.util.List;

public record CreateExamProblemsResponse(
    List<ExamProblemResponse> examProblemResponseList
) {
}
