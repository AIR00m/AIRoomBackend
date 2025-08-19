package com.airoom.airoom.common.exhandler;

import java.time.LocalDateTime;

public record ErrorResult(
        int status,
        String code,
        String message,
        LocalDateTime timeStamp
) {
    public ErrorResult(int status, String code, String message) {
        this(status, code, message, LocalDateTime.now());
    }
}
