package com.airoom.airoom.common.kafka.consumer;

import com.airoom.airoom.statistic.entity.LearningLog;
import com.airoom.airoom.statistic.entity.value.LogType;
import com.airoom.airoom.statistic.model.repository.LearningLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final LearningLogRepository learningLogRepository;
    private final List<LearningLog> buffer = Collections.synchronizedList(new ArrayList<>());

    private static final int BATCH_SIZE = 100;
    private static final long FLUSH_INTERVAL_MS = 60_000; // 1분마다 강제 flush
    private long lastFlushTime = System.currentTimeMillis();

    @KafkaListener(
            topics = {"exam-logs", "class-logs"},
            groupId = "learning-log-consumer",
            containerFactory = "kafkaBatchFactory"
    )
    public void consume(List<String> messages, Acknowledgment ack) {
        try {
            for (String message : messages) {
                try {
                    buffer.addAll(mapToEntities(message));
                } catch (Exception e) {
                    log.error("메시지 변환 실패 → 무시: {}", message, e);
                }
            }

            long now = System.currentTimeMillis();
            if (buffer.size() >= BATCH_SIZE || now - lastFlushTime >= FLUSH_INTERVAL_MS) {
                flush();
                ack.acknowledge();
                lastFlushTime = now;
            }

        } catch (Exception e) {
            log.error("consume() 처리 중 오류 발생 → 배치 전체 재시도", e);
        }
    }


    private void flush() {
        if (buffer.isEmpty()) return;

        List<LearningLog> toSave = new ArrayList<>(buffer);
        buffer.clear();
        learningLogRepository.saveAll(toSave); // JPA batch insert
        log.info("{}건 LearningLog 저장 완료", toSave.size());
    }

    private List<LearningLog> mapToEntities(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(message);

            if (!node.hasNonNull("llType")) {
                log.warn("llType이 null인 로그 무시: {}", message);
                return List.of();
            }

            String llType = node.get("llType").asText();
            LocalDateTime startTime = LocalDateTime.parse(node.get("llStartTime").asText().replace("Z", ""));
            LocalDateTime endTime   = LocalDateTime.parse(node.get("llEndTime").asText().replace("Z", ""));
            Long classroomStudentNo = node.get("classroomStudentNo").asLong();

            List<LearningLog> logs = new ArrayList<>();

            if (node.has("problemsData")) {
                // exam-logs
                for (JsonNode problem : node.withArray("problemsData")) {
                    logs.add(LearningLog.builder()
                            .llType(LogType.valueOf(llType))
                            .llStartTime(startTime)
                            .llEndTime(endTime)
                            .llDurationMs(Duration.ofSeconds(problem.get("llDurationSec").asLong()))
                            .selectedAnswer(problem.has("selectedAnswer") ? problem.get("selectedAnswer").asText(null) : null)
                            .anomalyCount(problem.has("anomalyCount") ? problem.get("anomalyCount").asInt() : 0)
                            .classroomStudentNo(classroomStudentNo)
                            .unitNo(problem.has("unitNo") && !problem.get("unitNo").isNull() ? problem.get("unitNo").asLong() : null)
                            .cepNo(problem.has("cepNo") ? problem.get("cepNo").asLong() : null)
                            .llIsCorrect(false)
                            .build());
                }
            } else {
                // class-logs
                logs.add(LearningLog.builder()
                        .llType(LogType.valueOf(llType))
                        .llStartTime(startTime)
                        .llEndTime(endTime)
                        .llDurationMs(Duration.ofSeconds(node.get("llDurationSec").asLong()))
                        .selectedAnswer(null)
                        .anomalyCount(0)
                        .classroomStudentNo(classroomStudentNo)
                        .unitNo(node.has("unitNo") && !node.get("unitNo").isNull() ? node.get("unitNo").asLong() : null)
                        .cepNo(null)
                        .llIsCorrect(false)
                        .build());
            }

            return logs;
        } catch (Exception e) {
            log.error("Kafka 메시지 매핑 실패: {}", message, e);
            return List.of(); // 실패한 메시지는 무시
        }
    }
}
