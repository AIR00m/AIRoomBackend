package com.airoom.airoom.aichat.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/aichat")
public class AiChatHealthController {

    private final WebClient openaiWebClient;
    private final WebClient qdrantWebClient;

    // 명시 생성자 + 파라미터 @Qualifier (필드의 @Qualifier/럼복 제거)
    public AiChatHealthController(
            @Qualifier("openaiWebClient") WebClient openaiWebClient,
            @Qualifier("qdrantWebClient") WebClient qdrantWebClient
    ) {
        this.openaiWebClient = openaiWebClient;
        this.qdrantWebClient = qdrantWebClient;
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        String openai = "ok";
        String qdrant = "ok";

        try {
            openaiWebClient.get().uri("/models").retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            openai = "fail:" + e.getClass().getSimpleName();
        }

        try {
            qdrantWebClient.get().uri("/collections").retrieve()
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
