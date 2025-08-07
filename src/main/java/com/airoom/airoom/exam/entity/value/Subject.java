package com.airoom.airoom.exam.entity.value;

import lombok.Getter;

@Getter
public enum Subject {
    MATH("math"),
    ENGLISH("english");

    private final String value;

    Subject(String value) {
        this.value = value;
    }

    public static Subject fromValue(String value) {
        for (Subject type : Subject.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("올바른 Subject 값이 아닙니다: " + value);
    }
}
