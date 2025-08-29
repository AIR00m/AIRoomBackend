package com.airoom.airoom.common.log.controller;

import com.airoom.airoom.common.log.model.dto.LogExamRequest;
import com.airoom.airoom.common.log.model.service.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
@Slf4j
public class LogController implements LogControllerSwagger{
    private final LogService logService;

    @PostMapping("/exam")
    public ResponseEntity<Void> logExamActivity(@Valid @RequestBody LogExamRequest request) {
        try {
            log.info("시험 로그 수신: examNo={}, classroomStudentNo={}",
                    request.examNo(), request.classroomStudentNo());

            logService.processExamLog(request);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("시험 로그 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}