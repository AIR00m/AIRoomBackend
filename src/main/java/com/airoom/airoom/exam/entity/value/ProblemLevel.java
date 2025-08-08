package com.airoom.airoom.exam.entity.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProblemLevel {
    HIGHEST(5, "최상"),
    HIGH(4, "상"),
    MEDIUM(3, "중"),
    LOW(2, "하"),
    LOWEST(1, "최하");

    private final int rank;
    private final String label;

    public static ProblemLevel fromLabel(String label) {
        for (ProblemLevel type : ProblemLevel.values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("올바른 ProblemLabel 값이 아닙니다: " + label);
    }
}
