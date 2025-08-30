package com.airoom.airoom.aichat.controller;

import com.airoom.airoom.aichat.model.AiProps;
import com.airoom.airoom.aichat.model.dto.SourceDto;
import com.airoom.airoom.aichat.model.service.OpenAiService;
import com.airoom.airoom.aichat.model.service.QdrantClient;
import com.airoom.airoom.aichat.model.service.StudentContextService;
import com.airoom.airoom.common.token.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aichat")
public class AiChatHealthController {

    private final WebClient openaiWebClient;
    private final WebClient qdrantWebClient;
    private final QdrantClient qdrant;
    private final OpenAiService openai;
    private final StudentContextService scs;
    private final AiProps props;

    public AiChatHealthController(
            @Qualifier("openaiWebClient") WebClient openaiWebClient,
            @Qualifier("qdrantWebClient") WebClient qdrantWebClient,
            QdrantClient qdrant,
            OpenAiService openai,
            StudentContextService scs,
            AiProps props
    ) {
        this.openaiWebClient = openaiWebClient;
        this.qdrantWebClient = qdrantWebClient;
        this.qdrant = qdrant;
        this.openai = openai;
        this.scs = scs;
        this.props = props;
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        String openai = "ok", qdrant = "ok";
        try {
            openaiWebClient.get().uri("/models").retrieve()
                    .bodyToMono(Map.class).timeout(java.time.Duration.ofSeconds(10)).block();
        } catch (Exception e) { openai = "fail:" + e.getClass().getSimpleName(); }

        try {
            qdrantWebClient.get().uri("/collections").retrieve()
                    .bodyToMono(Map.class).timeout(java.time.Duration.ofSeconds(5)).block();
        } catch (Exception e) { qdrant = "fail:" + e.getClass().getSimpleName(); }

        Map<String, String> res = new HashMap<>();
        res.put("openai", openai);
        res.put("qdrant", qdrant);
        return res;
    }

    // 1) 컬렉션 카운트
    @GetMapping("/health/qdrant-count")
    public Map<String,Object> count() {
        return Map.of("collection", props.getQdrant().getCollection(), "count", qdrant.count());
    }

    // 2) RAG 없이 드라이 검색 (임베딩+검색만)
    @GetMapping("/health/dry-search")
    public List<SourceDto> drySearch(@RequestParam String q,
                                     @RequestParam(defaultValue="5") int k) {
        var vec = openai.embed(q);
        return qdrant.search(vec, k, 0.2);
    }

    // 3) 로그인 사용자의 개인 컨텍스트 확인
    @GetMapping("/health/context")
    public Map<String,Object> context(@AuthenticationPrincipal CustomUserDetails me) {
        return scs.build(me.getMemberNo());
    }
}
