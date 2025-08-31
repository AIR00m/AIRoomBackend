/*
  Created by IntelliJ IDEA.
  User: poj23
  Date: 25. 8. 5.
  Time: 오전 9:22
*/
package com.airoom.airoom.common.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    /**
     * 시험 로그 전송
     */
    public void sendExamLog(Map<String, Object> logData) {
        try {
            String examNo = String.valueOf(logData.get("examNo"));

            log.info("📊 시험 로그 전송 시작: examNo={}", examNo);

            // exam-logs 토픽으로 전송 (logstash.conf에서 이미 설정됨)
            String topic = "exam-logs";
            String key = "exam-" + examNo;

            // JSON으로 변환
            String message = objectMapper.writeValueAsString(logData);

            // 비동기 전송
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("시험 로그 전송 완료: examNo={}, offset={}",
                            examNo, result.getRecordMetadata().offset());
                } else {
                    log.error("시험 로그 전송 실패: examNo={}, error={}",
                            examNo, ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("시험 로그 전송 예외 발생", e);
            throw new RuntimeException("시험 로그 전송 실패", e);
        }
    }

    public void sendClassLog(Map<String, Object> logData) {
        try {
            String unitNo = String.valueOf(logData.get("unitNo"));
            String studentNo = String.valueOf(logData.get("classroomStudentNo"));

            log.info("📚 학습 로그 전송 시작: unitNo={}, studentNo={}", unitNo, studentNo);

            // class-logs 토픽으로 전송
            String topic = "class-logs";
            String key = "unit-" + unitNo + "-student-" + studentNo;

            // JSON으로 변환
            String message = objectMapper.writeValueAsString(logData);

            // 비동기 전송
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("학습 로그 전송 완료: unitNo={}, studentNo={}, offset={}",
                            unitNo, studentNo, result.getRecordMetadata().offset());
                } else {
                    log.error("학습 로그 전송 실패: unitNo={}, studentNo={}, error={}",
                            unitNo, studentNo, ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("학습 로그 전송 예외 발생", e);
            throw new RuntimeException("학습 로그 전송 실패", e);
        }
    }
}