package com.airoom.airoom.aichat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.beans.factory.annotation.Qualifier;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/aichat")
@RequiredArgsConstructor
public class AiChatHealthController {

    // AiBeans에서 만든 두 클라이언트가 주입됩니다.
    private final @Qualifier("openaiWebClient") WebClient openAiWebClient;
    private final @Qualifier("qdrantWebClient") WebClient qdrantWebClient;

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        String openai = "ok";
        String qdrant = "ok";

        // OpenAI: 가벼운 API 호출 (models). 키가 잘못되면 401/403로 실패할 것.
        try {
            openAiWebClient.get()
                    .uri("/models")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            openai = "fail:" + e.getClass().getSimpleName();
        }

        // Qdrant: /collections GET
        try {
            qdrantWebClient.get()
                    .uri("/collections")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            qdrant = "fail:" + e.getClass().getSimpleName();
        }

        Map<String, String> res = new HashMap<>();
        res.put("openai", openai);
        res.put("qdrant", qdrant);
        return res;
    }
}
