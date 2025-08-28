package com.airoom.airoom.common.log.controller;

import com.airoom.airoom.common.log.model.dto.LogExamRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "로그 API", description = "시험 및 활동 로그 관련 API")
public interface LogControllerSwagger {

    @Operation(
            summary = "시험 활동 로그 전송",
            description = "시험 중 발생하는 모든 활동과 이상행위를 로그로 전송합니다."
    )
    public ResponseEntity<Void> logExamActivity(@Valid @RequestBody LogExamRequest request);
}