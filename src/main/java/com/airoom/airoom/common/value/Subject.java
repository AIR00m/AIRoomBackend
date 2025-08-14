package com.airoom.airoom.common.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Subject {
    MATH("수학"),
    ENGLISH("영어");

    private final String value;

    public static Subject fromValue(String value) {
        for (Subject type : Subject.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("올바른 Subject 값이 아닙니다: " + value);
    }
}
