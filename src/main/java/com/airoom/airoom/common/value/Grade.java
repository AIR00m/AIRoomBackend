package com.airoom.airoom.common.value;

public enum Grade {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    FIFTH(5),
    SIXTH(6);

    private final int value;

    Grade(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // 숫자로 enum 타입을 찾는 메소드
    public static Grade intFromValue(int value) {
        for (Grade g : Grade.values()) {
            if (g.getValue() == value) {
                return g;
            }
        }
        throw new
                IllegalArgumentException("해당 숫자의 학년이 존재하지 않습니다." + value);
    }
}

