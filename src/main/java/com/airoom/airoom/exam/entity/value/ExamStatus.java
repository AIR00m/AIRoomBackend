package com.airoom.airoom.exam.entity.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExamStatus {
    COMPLETE("완료"),
    INCOMPLETE("진행"),
    ALL("전체");

    private final String label;

    public static ExamStatus fromLabel(String label) {
        for (ExamStatus status : ExamStatus.values()) {
            if (status.label.equals(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("잘못된 ExamStatus 입니다. : " + label);
    }
}
