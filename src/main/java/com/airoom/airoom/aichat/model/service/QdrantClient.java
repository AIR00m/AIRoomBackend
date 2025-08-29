package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import com.airoom.airoom.aichat.model.dto.SourceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class QdrantClient {

    private final WebClient qdrantWebClient;
    private final AiProps props;

    @Data @AllArgsConstructor
    public static class Point {
        private String id;
        private List<Double> vector;
        private Map<String, Object> payload;
    }

    public void upsert(List<Point> points) {
        Map<String, Object> req = Map.of("points", points);
        qdrantWebClient.post()
                .uri("/collections/{col}/points", props.getQdrant().getCollection())
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(java.time.Duration.ofSeconds(15))
                .block(); // 서비스 경계에서 동기화
    }

    @SuppressWarnings("unchecked")
    public List<SourceDto> search(List<Double> queryVec, int topK,  Double threshold) {
        Map<String, Object> req = new HashMap<>();
        req.put("vector", queryVec);
        req.put("limit", topK);
        req.put("with_payload", true);
        req.put("with_vector", false);

        // 컷오프
        if (threshold != null) req.put("score_threshold", threshold);

        // 필터(lang=ko, grade=[1,2])
        Map<String,Object> filter = Map.of("must", List.of(
                Map.of("key","lang",  "match", Map.of("value","ko")),
                Map.of("key","grade", "match", Map.of("value",1)),
                Map.of("key","grade", "match", Map.of("value",2))
        ));
        req.put("filter", filter);

        Map<String, Object> res = qdrantWebClient.post()
                .uri("/collections/{col}/points/search", props.getQdrant().getCollection())
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(java.time.Duration.ofSeconds(15))
                .block();

        List<Map<String, Object>> rs = (List<Map<String, Object>>) res.get("result");
        List<SourceDto> out = new ArrayList<>();
        for (Map<String, Object> r : rs) {
            String id = String.valueOf(r.get("id"));
            double score = ((Number) r.get("score")).doubleValue();
            Map<String, Object> payload = (Map<String, Object>) r.get("payload");
            out.add(new SourceDto(id, score, payload));
        }
        return out;
    }
}
