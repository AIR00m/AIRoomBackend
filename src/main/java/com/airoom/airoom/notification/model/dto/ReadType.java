package com.airoom.airoom.notification.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReadType {

    Y("1"),
    N("0");
    
    private final String value;
    //생성자 대신 Required어노테이션
    
    public static ReadType fromValue(String value) {
        for (ReadType readType : ReadType.values()) {
            if (readType.value.equals(value)) {
                return readType;
            }
        }
        throw new IllegalArgumentException("올바른 ReadType 값이 아닙니다"+value);
    }
}
