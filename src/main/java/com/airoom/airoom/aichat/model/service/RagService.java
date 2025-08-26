package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import com.airoom.airoom.aichat.model.dto.AskRequest;
import com.airoom.airoom.aichat.model.dto.AskResponse;
import com.airoom.airoom.aichat.model.dto.SourceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RagService {

    private final AiProps props;
    private final OpenAiService openAiService;
    private final QdrantClient qdrantClient;

    public AskResponse ask(AskRequest req) {
        String user = Optional.ofNullable(req.getMessage()).orElse("").trim();
        if (user.isEmpty()) {
            return new AskResponse("질문이 비어있어요. 무엇이 궁금한가요?", List.of());
        }

        // 1) 사용자 질문 모더레이션
        if (openAiService.isFlagged(user)) {
            return new AskResponse("안전하지 않은 내용이 감지되어 답변할 수 없어요. 다른 질문을 해주세요.", List.of());
        }

        // 2) 임베딩 → 3) 검색
        List<Double> qvec = openAiService.embed(user);
        List<SourceDto> top = qdrantClient.search(qvec, props.getQdrant().getTopK());

        // 4) 컨텍스트 만들기
        StringBuilder ctx = new StringBuilder();
        for (int i = 0; i < top.size(); i++) {
            Map<String, Object> p = top.get(i).getPayload();
            String body = firstNonNullString(p, List.of("description", "content", "text", "body", "doc"));
            if (body == null) body = p.toString();
            ctx.append("### 문서 ").append(i + 1).append("\n")
                    .append(body).append("\n\n");
        }

        List<Map<String, String>> msgs = List.of(
                Map.of("role", "system", "content",
                        """
                        너는 한국어로 답변하는 진로/직업 도우미야.
                        반드시 아래 '컨텍스트' 범위에서만 답하고, 모르면 "해당 자료에서는 확실하지 않아요."라고 말해.
                        학생의 수준에 맞게 간결한 문단과 불릿으로 설명해.
                        답변 끝에 '출처 요약'으로 사용한 문서 번호만 나열해.
                        """),
                Map.of("role", "user", "content", "질문: " + user + "\n\n---\n컨텍스트:\n" + ctx)
        );

        // 5) 생성
        String answer = openAiService.chat(msgs);

        // 6) 생성물 모더레이션
        if (openAiService.isFlagged(answer)) {
            answer = "안전하지 않은 내용이 포함될 가능성이 있어 답변을 수정했어요. 다른 방식으로 질문을 시도해 주세요.";
        }

        // (선택) 여기서 메시지를 DB에 저장하면 됨.

        return new AskResponse(answer, top);
    }

    private String firstNonNullString(Map<String, Object> p, List<String> keys) {
        for (String k : keys) {
            Object v = p.get(k);
            if (v != null) return String.valueOf(v);
        }
        return null;
    }
}
