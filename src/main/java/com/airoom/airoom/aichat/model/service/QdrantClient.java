package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.*;

@Component
@RequiredArgsConstructor
public class QdrantClient {
    private final WebClient qdrantWebClient;
    private final AiProps props;

    @Getter @AllArgsConstructor
    public static class Hit {
        private String id;
        private double score;
        private Map<String, Object> payload;
    }

    public Mono<List<Hit>> search(List<Double> vector, int topK) {
        Map<String, Object> body = new HashMap<>();
        body.put("vector", vector);
        body.put("limit", topK);
        // 학년/언어 필터는 payload에 넣어두면 여기서 must 필터로 추가 가능
        return qdrantWebClient.post()
                .uri("/collections/{c}/points/search", props.getQdrant().getCollection())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    List result = (List) res.get("result");
                    List<Hit> hits = new ArrayList<>();
                    for (Object o : result) {
                        Map m = (Map) o;
                        String id = String.valueOf(m.get("id"));
                        double score = ((Number)m.get("score")).doubleValue();
                        Map<String,Object> payload = (Map<String,Object>) m.get("payload");
                        hits.add(new Hit(id, score, payload));
                    }
                    return hits;
                });
    }

    public Mono<Void> upsertBatch(List<Map<String,Object>> points) {
        Map<String,Object> body = Map.of("points", points);
        return qdrantWebClient.put()
                .uri("/collections/{c}/points?wait=true", props.getQdrant().getCollection())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
