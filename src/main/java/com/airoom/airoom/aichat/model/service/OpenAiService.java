package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenAiService {
    private final WebClient openAiWebClient;
    private final AiProps props;

    public Mono<List<Double>> embed(String text) {
        Map<String, Object> body = Map.of(
                "model", props.getOpenai().getEmbeddingModel(),
                "input", text
        );
        return openAiWebClient.post().uri("/embeddings")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> (List<Double>) ((Map)((List)res.get("data")).get(0)).get("embedding"));
    }

    public Mono<Boolean> isSafe(String text) {
        Map<String, Object> body = Map.of(
                "model", "omni-moderation-latest",   // 필요시 text-moderation-latest
                "input", text
        );
        return openAiWebClient.post().uri("/moderations")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    Map result = (Map)((List)res.get("results")).get(0);
                    Object flagged = result.get("flagged");
                    return !(flagged instanceof Boolean && (Boolean) flagged);
                });
    }

    public Mono<String> chat(String system, String user) {
        Map<String, Object> body = Map.of(
                "model", props.getOpenai().getChatModel(),
                "messages", List.of(
                        Map.of("role","system","content",system),
                        Map.of("role","user","content",user)
                ),
                "temperature", 0.3,
                "max_tokens", 300
        );
        return openAiWebClient.post().uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    List choices = (List)res.get("choices");
                    Map choice0 = (Map)choices.get(0);
                    Map msg = (Map)choice0.get("message");
                    return (String) msg.get("content");
                });
    }
}
