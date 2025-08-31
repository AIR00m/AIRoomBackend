package com.airoom.airoom.common.log.model.service;

import com.airoom.airoom.common.kafka.producer.KafkaProducerService;
import com.airoom.airoom.common.log.model.dto.LogExamRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    /**
     * milliseconds timestamp를 ISO 형식으로 변환
     */
    private String convertTimestampToISO(Long timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}