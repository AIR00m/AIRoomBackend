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
import org.springframework.scheduling.annotation.Scheduled;
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

    @KafkaListener(topics = {"exam-logs", "class-logs"}, groupId = "learning-log-consumer", containerFactory = "kafkaBatchFactory")
    public void consume(List<String> messages, Acknowledgment ack) {
        for (String message : messages) {
            buffer.addAll(mapToEntities(message));
        }
        if (buffer.size() >= BATCH_SIZE) {
            flush();
            ack.acknowledge();
        }
    }

    @Scheduled(fixedRate = 60000) // 1분마다 배치 처리
    public void flushByTime() {
        if (!buffer.isEmpty()) {
            flush();
        }
    }

    private void flush() {
        List<LearningLog> toSave = new ArrayList<>(buffer);
        buffer.clear();
        learningLogRepository.saveAll(toSave); // JPA batch insert
        log.info("{}건 LearningLog 저장 완료", toSave.size());
    }

    private List<LearningLog> mapToEntities(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(message);

            String llType = node.get("llType").asText();
            LocalDateTime startTime = LocalDateTime.parse(node.get("llStartTime").asText());
            LocalDateTime endTime   = LocalDateTime.parse(node.get("llEndTime").asText());

            Long classroomStudentNo = node.get("classroomStudentNo").asLong();

            List<LearningLog> logs = new ArrayList<>();

            if (node.has("problemsData")) {
                // exam-logs
                for (JsonNode problem : node.withArray("problemsData")) {
                    logs.add(LearningLog.builder()
                            .llType(LogType.valueOf(llType))
                            .llStartTime(startTime)
                            .llEndTime(endTime)
                            .llDurationSec(Duration.ofSeconds(problem.get("llDurationSec").asLong()))
                            .selectedAnswer(problem.has("selectedAnswer") ? problem.get("selectedAnswer").asText(null) : null)
                            .anomalyCount(problem.has("anomalyCount") ? problem.get("anomalyCount").asInt() : 0)
                            .classroomStudentNo(classroomStudentNo)
                            .unitNo(problem.has("unitNo") && !problem.get("unitNo").isNull() ? problem.get("unitNo").asLong() : null)
                            .cepNo(problem.has("cepNo") ? problem.get("cepNo").asLong() : null)
                            .llIsCorrect(false)
                            .build());
                }
            }
            else {
                // class-logs
                logs.add(LearningLog.builder()
                        .llType(LogType.valueOf(llType))
                        .llStartTime(startTime)
                        .llEndTime(endTime)
                        .llDurationSec(Duration.ofSeconds(node.get("llDurationSec").asLong()))
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
            throw new RuntimeException("Kafka 메시지 매핑 실패: " + message, e);
        }
    }
}
