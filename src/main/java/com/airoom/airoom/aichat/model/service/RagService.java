package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.model.AiProps;
import com.airoom.airoom.aichat.model.dto.AskRequest;
import com.airoom.airoom.aichat.model.dto.AskResponse;
import com.airoom.airoom.aichat.model.dto.SourceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final AiProps props;
    private final OpenAiService openAiService;
    private final QdrantClient qdrantClient;

    public AskResponse ask(AskRequest req) {
        String user = Optional.ofNullable(req.getMessage()).orElse("").trim();
        if (user.isEmpty()) {
            return new AskResponse("질문이 비어있어요. 무엇이 궁금한가요?", List.of(), List.of());
        }

        // ---- (선택) 학생 컨텍스트 (Controller/Service에서 주입 가능) ----
        Map<String, Object> ctxMap = Optional.ofNullable(req.getContext()).orElseGet(Map::of);
        String studentProfile = buildStudentProfile(ctxMap);

        if (!ctxMap.isEmpty() && user.matches(".*(학교 이름|몇 ?반|학급|클래스).*")) {
            String school = String.valueOf(ctxMap.getOrDefault("school", ""));
            Object clazz = ctxMap.get("class");
            String klass = (clazz == null ? "" : (clazz + "반"));
            String direct = (school.isBlank() && klass.isBlank())
                    ? "등록된 학교/반 정보를 찾지 못했어요. 마이페이지에서 프로필을 확인해 주세요."
                    : String.format("%s %s이에요. 😊", school, klass).trim();
            return new AskResponse(direct, List.of(), List.of());
        }

        try {
            // 1) 질문 모더레이션
            log.info("Starting moderation check for: {}", user);
            if (openAiService.isFlagged(user)) {
                return new AskResponse("안전하지 않은 내용이 감지되어 답변할 수 없어요. 다른 질문을 해주세요.", List.of(), List.of());
            }

            // 2) 임베딩
            log.info("Starting embedding for: {}", user);
            List<Double> qvec = openAiService.embed(user);
            log.info("Embedding completed, vector size: {}", qvec.size());

            // 3) Qdrant 검색
            log.info("Starting Qdrant search");
            List<SourceDto> top = qdrantClient.search(qvec, props.getQdrant().getTopK(), 0.2);
            log.info("Qdrant search completed, found {} results", top != null ? top.size() : 0);

            // 4) 컨텍스트 & 인덱스 매핑
            String ctx = buildContextBlock(top);
            List<AskResponse.SourceIndex> indices = new ArrayList<>();
            for (int i = 0; i < (top == null ? 0 : top.size()); i++) {
                var s = top.get(i);
                var title = String.valueOf(s.getPayload().getOrDefault("title",""));
                indices.add(new AskResponse.SourceIndex(i+1, s.getId(), title));
            }

            // 5) 메시지
            List<Map<String, String>> msgs =
                    (top == null || top.isEmpty())
                            ? List.of(
                            Map.of("role","system","content", systemPromptFallback(studentProfile)),
                            Map.of("role","user","content", user)
                    )
                            : List.of(
                            Map.of("role","system","content", systemPromptWithContext(studentProfile)),
                            Map.of("role","user","content", "질문: " + user + "\n\n---\n컨텍스트:\n" + ctx)
                    );

            String answer = openAiService.chat(msgs);

            // 6) 생성물 모더레이션
            if (openAiService.isFlagged(answer)) {
                answer = "안전하지 않은 내용이 포함될 가능성이 있어 답변을 수정했어요. 다른 방식으로 질문을 시도해 주세요.";
            }

            return new AskResponse(answer, (top == null ? List.of() : top), List.of());

        } catch (Exception e) {
            log.error("Error in RAG service for question: {}", user, e);
            // 폴백 처리
            try {
                List<Map<String, String>> fb = List.of(
                        Map.of("role","system","content", systemPromptFallback(studentProfile)),
                        Map.of("role","user","content", user)
                );
                String answer = openAiService.chat(fb);
                if (openAiService.isFlagged(answer)) {
                    answer = "안전하지 않은 내용이 포함될 가능성이 있어 답변을 수정했어요. 다른 방식으로 질문을 시도해 주세요.";
                }
                return new AskResponse(answer, List.of(), List.of());
            } catch (Exception fallbackException) {
                log.error("Fallback also failed for question: {}", user, fallbackException);
                return new AskResponse("죄송해요, 지금 답변을 드릴 수 없어요. 잠시 후 다시 시도해 주세요.", List.of(), List.of());
            }
        }
    }

    /* ---------- 프롬프트 ---------- */

    private String systemPromptWithContext(String studentProfile) {
        return """
                너는 한국어로 답변하는 **초등학교 1~2학년 전용** 학습 도우미야.
                말투는 밝고 다정하고 부드럽고 친절한 존댓말을 사용하고, 어려운 말은 쓰지 말고 쉬운 어휘로 설명해줘.
                이모지는 너무 많이 쓰지 말고 ✨, 😊 정도만 가끔 사용해.
                문장은 짧고, 핵심을 불릿으로 정리하고, 아주 간단한 예시(생활 속 비유)를 1개 정도 포함해.
                먼저 '컨텍스트'에서 근거를 찾아 답하고, 부족하면 일반 교과 상식으로 보충하되 추측은 하지 않아.
                
                [학생 프로필]
                %s
                """.formatted(studentProfile == null || studentProfile.isBlank() ? "제공된 정보 없음" : studentProfile);
    }

    private String systemPromptFallback(String studentProfile) {
        return """
                너는 한국어로 답변하는 **초등학교 1~2학년 전용** 학습 도우미야.
                말투는 밝고 다정하고 부드럽고 친절한 존댓말을 사용하고, 어려운 말은 쓰지 말고 쉬운 어휘로 설명해줘.
                이모지는 너무 많이 쓰지 말고 ✨, 😊 정도만 가끔 사용해.
                문장은 짧고, 핵심을 불릿으로 정리하고, 아주 간단한 예시(생활 속 비유)를 1개 정도 포함해.
                추측은 하지 말고, 모르면 모른다고 말해.
                
                [학생 프로필]
                %s
                """.formatted(studentProfile == null || studentProfile.isBlank() ? "제공된 정보 없음" : studentProfile);
    }

    /* ---------- 컨텍스트/프로필 빌더 ---------- */

    private String buildContextBlock(List<SourceDto> top) {
        if (top == null || top.isEmpty()) return "";
        StringBuilder ctx = new StringBuilder();
        for (int i = 0; i < top.size(); i++) {
            Map<String, Object> p = top.get(i).getPayload();
            String body = firstNonNullString(p, List.of("description","content","text","body","doc"));
            if (body == null) body = p.toString();
            ctx.append("### 문서 ").append(i + 1).append("\n")
                    .append(body).append("\n\n");
        }
        return ctx.toString();
    }

    private String buildStudentProfile(Map<String, Object> ctx) {
        if (ctx == null || ctx.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        appendIfPresent(sb, ctx, "memberNo", "회원번호");
        appendIfPresent(sb, ctx, "name", "이름");
        appendIfPresent(sb, ctx, "gender", "성별");
        appendIfPresent(sb, ctx, "school", "학교");
        appendIfPresent(sb, ctx, "grade", "학년");
        appendIfPresent(sb, ctx, "class", "반");
        appendIfPresent(sb, ctx, "classroomGrade", "클래스 학년");
        appendIfPresent(sb, ctx, "classroomNo", "클래스 번호");

        Object scores = ctx.get("recentScores");
        if (scores != null) sb.append("- 최근 점수: ").append(scores).append("\n");
        return sb.toString().trim();
    }
    private void appendIfPresent(StringBuilder sb, Map<String, Object> ctx, String key, String label){
        Object v = ctx.get(key);
        if (v != null) sb.append("- ").append(label).append(": ").append(v).append("\n");
    }

    private String firstNonNullString(Map<String, Object> p, List<String> keys) {
        for (String k : keys) {
            Object v = p.get(k);
            if (v != null) return String.valueOf(v);
        }
        return null;
    }
}
