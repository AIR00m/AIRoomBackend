package com.airoom.airoom.common.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Semester {
    First(1),
    Second(2);

    private final int value;

    public static Semester fromValue(int value) {
        for (Semester type : Semester.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("올바른 Semester 값이 아닙니다." + value);
    }
}
