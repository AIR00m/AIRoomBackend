package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final WebClient openaiWebClient;
    private final AiProps props;

    @SuppressWarnings("unchecked")
    public List<Double> embed(String text) {
        try {
            Map<String, Object> req = Map.of(
                    "model", props.getOpenai().getEmbeddingModel(),
                    "input", text
            );
            Map<String, Object> res = openaiWebClient.post()
                    .uri("/embeddings")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofSeconds(20))
                    .block();

            if (res == null || !res.containsKey("data")) {
                throw new RuntimeException("Invalid OpenAI embedding response: " + res);
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) res.get("data");
            if (data == null || data.isEmpty()) {
                throw new RuntimeException("Empty data in OpenAI embedding response");
            }

            Map<String, Object> first = data.get(0);
            List<Double> embedding = (List<Double>) first.get("embedding");
            if (embedding == null || embedding.isEmpty()) {
                throw new RuntimeException("No embedding vector in OpenAI response");
            }

            return embedding;
        } catch (Exception e) {
            log.error("OpenAI embedding failed for text: {}", text, e);
            throw new RuntimeException("OpenAI embedding service error", e);
        }
    }


    public boolean isFlagged(String text) {
        Map<String, Object> req = Map.of(
                "model", "omni-moderation-latest",
                "input", text
        );
        Map<?, ?> res = openaiWebClient.post()
                .uri("/moderations")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(java.time.Duration.ofSeconds(20))
                .block();

        List<Map<String, Object>> results = (List<Map<String, Object>>) res.get("results");
        return (Boolean) results.get(0).get("flagged");
    }

    @SuppressWarnings("unchecked")
    public String chat(List<Map<String, String>> messages) {
        Map<String, Object> req = Map.of(
                "model", props.getOpenai().getChatModel(),
                "messages", messages,
                "temperature", 0.65,            // ← 톤을 조금 더 밝고 자연스럽게
                "max_tokens", 700               // ← 과도한 장문 방지
        );
        Map<String, Object> res = openaiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(java.time.Duration.ofSeconds(60))
                .block();

        Map<String, Object> choice0 = ((List<Map<String, Object>>) res.get("choices")).get(0);
        Map<String, Object> msg = (Map<String, Object>) choice0.get("message");
        return (String) msg.get("content");
    }
}