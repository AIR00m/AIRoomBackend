package com.airoom.airoom.exam.entity.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProblemLevel {
    HIGH(3, "상"),
    MEDIUM(2, "중"),
    LOW(1, "하");

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
