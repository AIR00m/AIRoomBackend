package com.airoom.airoom.exam.model.dto;

import com.airoom.airoom.exam.entity.value.ProblemLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ExamProblemRequest(
        @NotNull(message = "단원 교유번호는 필수입니다.")
        Long unitNo, //단원 고유번호

        @NotEmpty(message = "최소 1개 이상의 난이도 문제수가 필요합니다.")
        Map<ProblemLevel,
                @NotNull(message = "문제 수는 필수입니다.")
                @Min(value = 0, message = "문제 수는 0 이상이어야 합니다.")
                        Integer> problemCountsByLevel //난이도별 문제수
) {
}
