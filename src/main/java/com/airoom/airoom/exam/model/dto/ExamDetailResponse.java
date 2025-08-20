package com.airoom.airoom.exam.model.dto;

import java.util.List;

public record ExamDetailResponse(
        Long examNo,
        List<ExamProblemDetailResponse> examProblemDetailResponseList
) {
}
