package com.airoom.airoom.exam.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public record StudentAnswerRequest(
        @NotNull(message = "시험문제 고유번호는 필수입니다.")
        Long epNo,

        @NotNull(message = "시험출제문제 고유번호는 필수입니다.")
        Long cepNo,

        @NotEmpty(message = "제출응답은 필수입니다.")
        String saAnswer,

        Duration saSolvingTime
) {
}
