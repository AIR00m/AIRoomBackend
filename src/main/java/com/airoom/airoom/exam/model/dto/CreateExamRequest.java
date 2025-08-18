package com.airoom.airoom.exam.model.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record CreateExamRequest(
        @NotNull(message = "시험 이름은 필수입니다.")
        String examName,

        @NotNull(message = "시험 범위는 필수입니다.")
        List<Integer> unitNoList,

        @NotNull(message = "시험 문제 수는 필수입니다.")
        Integer examProblemCount,

        LocalDateTime examStartTime,

        LocalDateTime examEndTime
) {
}
