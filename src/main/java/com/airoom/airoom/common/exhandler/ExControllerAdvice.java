package com.airoom.airoom.common.exhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExControllerAdvice {
    // 잘못된 파라미터 등 클라이언트 오류
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResult> handleIllegalArgument(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] {}", e.getMessage(), e);

        ErrorResult errorResult = new ErrorResult(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
    }

    // 예상 못한 모든 예외 처리 (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception e) {
        log.error("[Unexpected Exception] {}", e.getMessage(), e);

        ErrorResult errorResult = new ErrorResult(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "서버 내부 오류가 발생했습니다."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
}
