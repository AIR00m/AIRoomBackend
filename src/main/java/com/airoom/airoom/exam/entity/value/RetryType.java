package com.airoom.airoom.exam.entity.value;

import lombok.Getter;

@Getter
public enum RetryType {
    Y("1"),
    N("0");

    private final String value;

    RetryType(String value) {
        this.value = value;
    }

    public static RetryType fromValue(String value) {
        for (RetryType type : RetryType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("올바른 RetryType 값이 아닙니다: " + value);
    }
}
