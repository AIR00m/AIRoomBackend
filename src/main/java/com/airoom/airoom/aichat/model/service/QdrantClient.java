package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import com.airoom.airoom.aichat.model.dto.SourceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
        private String id;
        private List<Double> vector;
        private Map<String, Object> payload;
    }

    // 캐시된 스키마
    private volatile Optional<CollectionSchema> cachedSchema = Optional.empty();

    @Data
    static class CollectionSchema {
        // namedVectors가 비어있지 않으면 이름 있는 벡터
        Map<String, VectorConf> namedVectors; // 예: {"text": {size:1536,...}}
        VectorConf singleVector;              // 예: {size:1536,...} (기본 벡터)
        boolean isNamed() { return namedVectors != null && !namedVectors.isEmpty(); }
        String firstVectorName() { return isNamed() ? namedVectors.keySet().iterator().next() : null; }
        int dimension() {
            if (isNamed()) return namedVectors.values().iterator().next().getSize();
            return singleVector.getSize();
        }
    }
    @Data
    static class VectorConf { int size; String distance; }

    private CollectionSchema describeCollection() {
        return cachedSchema.orElseGet(() -> {
            Map res = qdrantWebClient.get()
                    .uri("/collections/{col}", props.getQdrant().getCollection())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            // JSON 파싱 최소화: 필요한 부분만 안전하게 꺼내기
            Map result = (Map) res.get("result");
            Map config = (Map) result.get("config");
            Map params = (Map) config.get("params");
            Object vectors = params.get("vectors");

            CollectionSchema schema = new CollectionSchema();
            if (vectors instanceof Map) {
                // 이름 있는 벡터인 경우: {"text": {...}} 형태
                Map<String,Object> mv = (Map<String,Object>) vectors;
                boolean looksSingle = mv.containsKey("size") && mv.containsKey("distance");
                if (looksSingle) {
                    // 단일 벡터
                    VectorConf vc = new VectorConf();
                    vc.setSize(((Number) mv.get("size")).intValue());
                    vc.setDistance(String.valueOf(mv.get("distance")));
                    schema.setSingleVector(vc);
                } else {
                    Map<String,VectorConf> named = new LinkedHashMap<>();
                    for (var e : mv.entrySet()) {
                        Map val = (Map) e.getValue();
                        VectorConf vc = new VectorConf();
                        vc.setSize(((Number) val.get("size")).intValue());
                        vc.setDistance(String.valueOf(val.get("distance")));
                        named.put(e.getKey(), vc);
                    }
                    schema.setNamedVectors(named);
                }
            } else {
                throw new IllegalStateException("Unexpected vectors format from Qdrant");
            }
            cachedSchema = Optional.of(schema);
            log.info("[Qdrant] schema loaded: named={}, dim={}, name={}",
                    schema.isNamed(), schema.dimension(), schema.firstVectorName());
            return schema;
        });
    }

    /** wait=true 업서트 (스키마에 맞춰 포맷 자동 선택, 에러 바디 항상 로깅) */
    public void upsertWait(List<Point> points) {
        var schema = describeCollection();

        // 1) points 포맷 시도
        Map<String, Object> pointsBody = Map.of(
                "points", points.stream().map(p -> {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    if (schema.isNamed()) {
                        m.put("vector", Map.of(schema.firstVectorName(), p.getVector()));
                    } else {
                        m.put("vector", p.getVector());
                    }
                    m.put("payload", p.getPayload());
                    return m;
                }).toList()
        );

        try {
            qdrantWebClient.put()
                    .uri("/collections/{col}/points?wait=true", props.getQdrant().getCollection())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(pointsBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            return;
        } catch (WebClientResponseException e) {
            log.error("[Qdrant] upsert(points) 400 body={}", e.getResponseBodyAsString());
        }

        // 2) batch 포맷 재시도
        List<Object> ids = new ArrayList<>(points.size());
        List<List<Double>> vectors = new ArrayList<>(points.size());
        List<Map<String, Object>> payloads = new ArrayList<>(points.size());
        for (Point p : points) {
            ids.add(p.getId());
            vectors.add(p.getVector());
            payloads.add(p.getPayload());
        }

        Map<String,Object> batchBody = schema.isNamed()
                ? Map.of("batch", Map.of(
                "ids", ids,
                "vectors", Map.of(schema.firstVectorName(), vectors),
                "payloads", payloads
        ))
                : Map.of("batch", Map.of(
                "ids", ids,
                "vectors", vectors,
                "payloads", payloads
        ));

        qdrantWebClient.put()
                .uri("/collections/{col}/points?wait=true", props.getQdrant().getCollection())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(batchBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    public List<SourceDto> search(List<Double> queryVec, int topK, Double threshold) {
        List<SourceDto> out = searchOnce(queryVec, topK, (threshold == null ? 0.20 : threshold), true);
        if (out.isEmpty()) {
            log.warn("[Qdrant] no hits with lang filter; retrying without filter (lower threshold)");
            out = searchOnce(queryVec, topK, 0.10, false);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<SourceDto> searchOnce(List<Double> queryVec, int topK, double threshold, boolean useLangFilter) {
        var schema = describeCollection();

        Map<String, Object> req = new HashMap<>();
        req.put("vector", queryVec);
        req.put("limit", topK);
        req.put("with_payload", true);
        req.put("with_vector", false);
        req.put("score_threshold", threshold);

        // named vector 컬렉션 대비
        if (schema.isNamed()) {
            req.put("using", schema.firstVectorName());
        }

        if (useLangFilter) {
            Map<String,Object> langMust = Map.of("key","lang", "match", Map.of("value","ko"));
            Map<String,Object> grade1 = Map.of("key","grade", "match", Map.of("value", 1));
            Map<String,Object> grade2 = Map.of("key","grade", "match", Map.of("value", 2));
            Map<String,Object> filter = new HashMap<>();
            filter.put("must", List.of(langMust));
            filter.put("should", List.of(grade1, grade2)); // 1 또는 2
            req.put("filter", filter);
        }

        Map<String, Object> res;
        try {
            res = qdrantWebClient.post()
                    .uri("/collections/{col}/points/search", props.getQdrant().getCollection())
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[Qdrant] search 5xx/4xx body={}", e.getResponseBodyAsString());
            throw e;
        }

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

    @SuppressWarnings("unchecked")
    public int countByFilter(Map<String, Object> filter) {
        Map<String,Object> body = Map.of("filter", filter == null ? Map.of() : filter);
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
