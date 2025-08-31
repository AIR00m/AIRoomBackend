package com.airoom.airoom.common.log.model.service;

import com.airoom.airoom.common.kafka.producer.KafkaProducerService;
import com.airoom.airoom.common.log.model.dto.LogClassRequest;
import com.airoom.airoom.common.log.model.dto.LogExamRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final KafkaProducerService kafkaProducerService;

    public void processExamLog(LogExamRequest request) {
        try {

            if (request.examNo() == null || request.classroomStudentNo() == null) {
                log.error("🚨 필수 파라미터 누락: examNo={}, classroomStudentNo={}",
                        request.examNo(), request.classroomStudentNo());
                throw new IllegalArgumentException("examNo와 classroomStudentNo는 필수 값입니다.");
            }

            Map<String, Object> logData = new HashMap<>();

            logData.put("examNo", request.examNo());
            logData.put("classroomStudentNo", request.classroomStudentNo());
            logData.put("llType", request.llType());
            logData.put("llStartTime", request.llStartTime());
            logData.put("llEndTime", request.llEndTime());

            logData.put("problemsData", request.problemsData());

            // Kafka로 전송 (exam-logs 토픽)
            kafkaProducerService.sendExamLog(logData);

            log.info("시험 로그 처리 완료: examNo={}, eventType={}",
                    request.examNo(), logData.get("eventType"));

        } catch (Exception e) {
            log.error("시험 로그 처리 실패: examNo={}, error={}",
                    request.examNo(), e.getMessage(), e);
            throw new RuntimeException("시험 로그 처리 실패", e);
        }
    }

    public void processClassLog(LogClassRequest request) {
        try {
            if (request.unitNo() == null || request.classroomStudentNo() == null) {
                log.error("🚨 필수 파라미터 누락: unitNo={}, classroomStudentNo={}",
                        request.unitNo(), request.classroomStudentNo());
                throw new IllegalArgumentException("unitNo와 classroomStudentNo는 필수 값입니다.");
            }

            Map<String, Object> logData = new HashMap<>();

            // 기본 정보
            logData.put("unitNo", request.unitNo());
            logData.put("classroomStudentNo", request.classroomStudentNo());
            logData.put("llType", request.llType() != null ? request.llType() : "LEARN");

            // 시간 정보
            logData.put("llStartTime", request.llStartTime());
            logData.put("llEndTime", request.llEndTime());
            logData.put("llDurationSec", request.llDurationSec());

            // class-logs 토픽으로 전송
            kafkaProducerService.sendClassLog(logData);

            log.info("학습 로그 처리 완료: unitNo={}, duration={}ms",
                    request.unitNo(), request.llDurationSec());

        } catch (Exception e) {
            log.error("학습 로그 처리 실패: unitNo={}, error={}",
                    request.unitNo(), e.getMessage(), e);
            throw new RuntimeException("학습 로그 처리 실패", e);
        }
    }

}