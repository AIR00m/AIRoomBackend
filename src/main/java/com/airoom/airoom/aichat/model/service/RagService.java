package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import com.airoom.airoom.aichat.model.dto.SourceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {
    private final OpenAiService openAi;
    private final QdrantClient qdrant;
    private final AiProps props;

    private static final String SYSTEM = """
            너는 초등학교 1~2학년에게 직업을 쉽게 설명하는 한국어 도우미야.
            항상 2~4문장, 짧고 쉬운 말로 답해. 어려운 단어는 풀어 말해.
            위험/부적절한 주제는 답하지 말고 '선생님께 물어보자'라고 안내해.
            """;

    public Mono<Result> answer(String question) {
        // 1) moderation(입력)
        return openAi.isSafe(question).flatMap(isOk -> {
            if (!isOk) {
                return Mono.just(new Result("그 질문은 안전하지 않아요. 선생님께 물어보자!", List.of()));
            }
            // 2) embed -> 3) search -> 4) compose -> 5) chat -> 6) moderation(출력)
            return openAi.embed(question)
                    .flatMap(vec -> qdrant.search(vec, props.getQdrant().getTopK()))
                    .flatMap(hits -> {
                        String context = hits.stream().limit(3)
                                .map(h -> {
                                    Object title = h.getPayload() == null ? "" : h.getPayload().getOrDefault("title", "");
                                    Object body  = h.getPayload() == null ? "" : h.getPayload().getOrDefault("body", "");
                                    return "- " + title + "\n" + body;
                                })
                                .collect(Collectors.joining("\n\n"));

                        String userMsg = "질문: " + question +
                                "\n\n도움이 될 자료(필요할 때만 참고):\n" + context;

                        return openAi.chat(SYSTEM, userMsg)
                                .flatMap(ans -> openAi.isSafe(ans)
                                        .map(ok -> ok ? ans : "이 내용은 여기서 다루기 어려워요. 선생님께 물어보자!"))
                                .map(ans -> new Result(ans, hits.stream().map(h ->
                                                new SourceDto(h.getId(),
                                                        String.valueOf(h.getPayload()==null? "" : h.getPayload().getOrDefault("title","")),
                                                        h.getScore()))
                                        .collect(Collectors.toList())));
                    });
        });
    }

    public record Result(String answer, List<SourceDto> sources) {}
}
