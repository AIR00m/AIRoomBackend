package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import com.airoom.airoom.aichat.model.dto.SourceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class QdrantClient {

    private final WebClient qdrantWebClient;
    private final AiProps props;

    @Data @AllArgsConstructor
    public static class Point {
        private String id;                    // UUID 문자열 사용
        private List<Double> vector;
        private Map<String, Object> payload;
    }

    /** wait=true 로 업서트하고, 실패 시 에러 바디 로깅 */
    public void upsertWait(List<Point> points) {
        // 1) points -> batch 포맷으로 변환
        List<String> ids = points.stream().map(Point::getId).collect(Collectors.toList());
        List<List<Double>> vectors = points.stream().map(Point::getVector).collect(Collectors.toList());
        List<Map<String,Object>> payloads = points.stream().map(Point::getPayload).collect(Collectors.toList());

        Map<String, Object> batch = Map.of(
                "ids", ids,
                "vectors", vectors,
                "payloads", payloads
        );
        Map<String, Object> req = Map.of("batch", batch);

        // 2) 업서트 (wait=true) + 에러바디 로깅
        try {
            qdrantWebClient.post()
                    .uri("/collections/{col}/points?wait=true", props.getQdrant().getCollection())
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[Qdrant] upsert failed {} body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public List<SourceDto> search(List<Double> queryVec, int topK, Double threshold) {
        Map<String, Object> req = new HashMap<>();
        req.put("vector", queryVec);
        req.put("limit", topK);
        req.put("with_payload", true);
        req.put("with_vector", false);
        if (threshold != null) req.put("score_threshold", threshold);

        // 필터(lang=ko AND grade any of [1,2])
        Map<String,Object> langMust = Map.of("key","lang", "match", Map.of("value","ko"));

        // payload.grade 가 [1,2] 배열이므로 any 사용 (대안: must+should로 1 or 2)
        Map<String,Object> gradeAny = Map.of("key","grade", "match", Map.of("any", List.of(1,2)));

        Map<String,Object> filter = Map.of("must", List.of(langMust, gradeAny));
        req.put("filter", filter);

        Map<String, Object> res = qdrantWebClient.post()
                .uri("/collections/{col}/points/search", props.getQdrant().getCollection())
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .block();

        List<Map<String, Object>> rs = (List<Map<String, Object>>) res.get("result");
        List<SourceDto> out = new ArrayList<>();
        if (rs != null) {
            for (Map<String, Object> r : rs) {
                String id = String.valueOf(r.get("id"));
                double score = ((Number) r.get("score")).doubleValue();
                Map<String, Object> payload = (Map<String, Object>) r.get("payload");
                out.add(new SourceDto(id, score, payload));
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public int count() {
        Map<String,Object> body = Map.of("filter", Map.of()); // 전체 카운트
        Map<String,Object> res = qdrantWebClient.post()
                .uri("/collections/{col}/points/count?exact=true", props.getQdrant().getCollection())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        Map<String,Object> result = (Map<String,Object>) res.get("result");
        return result == null ? 0 : ((Number) result.get("count")).intValue();
    }
}
