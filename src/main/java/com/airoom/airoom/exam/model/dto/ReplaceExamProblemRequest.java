package com.airoom.airoom.exam.model.dto;

import jakarta.validation.constraints.NotNull;

public record ReplaceExamProblemRequest(
        @NotNull(message = "시험문제 고유번호는 필수입니다.")
        Long epNo
) {
}
