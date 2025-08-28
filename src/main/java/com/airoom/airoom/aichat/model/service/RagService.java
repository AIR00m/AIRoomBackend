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

        // (선택) 프론트/백엔드 어디서든 넣어줄 수 있는 학생 컨텍스트
        // 예: req.context = { memberNo: 3, grade: 2, readingLevel: "초저", interests:["축구"], recentScores:{math:82}}
        Map<String, Object> ctxMap = Optional.ofNullable(req.getContext()).orElseGet(Map::of);
        String studentProfile = buildStudentProfile(ctxMap); // ↓ 아래 헬퍼가 텍스트로 풀어줌

        // 1) 질문 모더레이션
        if (openAiService.isFlagged(user)) {
            return new AskResponse("안전하지 않은 내용이 감지되어 답변할 수 없어요. 다른 질문을 해주세요.", List.of());
        }

        try {
            // 2) 임베딩 → 3) 검색
            List<Double> qvec = openAiService.embed(user);
            List<SourceDto> top = qdrantClient.search(qvec, props.getQdrant().getTopK());

            // 4) 컨텍스트 문자열
            String ctx = buildContextBlock(top);

            // 5) 메시지 구성
            List<Map<String, String>> msgs;
            if (top == null || top.isEmpty()) {
                // 컨텍스트 없음 → 폴백: 일반 지식으로 초1·초2 톤 답변
                msgs = List.of(
                        Map.of("role","system","content", systemPromptFallback(studentProfile)),
                        Map.of("role","user","content", user)
                );
            } else {
                // 컨텍스트 있음 → 컨텍스트 우선 + 부족하면 보충
                msgs = List.of(
                        Map.of("role","system","content", systemPromptWithContext(studentProfile)),
                        Map.of("role","user","content", "질문: " + user + "\n\n---\n컨텍스트:\n" + ctx)
                );
            }

            String answer = openAiService.chat(msgs);

            // 6) 생성물 모더레이션
            if (openAiService.isFlagged(answer)) {
                answer = "안전하지 않은 내용이 포함될 가능성이 있어 답변을 수정했어요. 다른 방식으로 질문을 시도해 주세요.";
            }

            return new AskResponse(answer, (top == null ? List.of() : top));

        } catch (Exception e) {
            // 예외 시에도 폴백
            List<Map<String, String>> fb = List.of(
                    Map.of("role","system","content", systemPromptFallback(studentProfile)),
                    Map.of("role","user","content", user)
            );
            String answer = openAiService.chat(fb);
            if (openAiService.isFlagged(answer)) {
                answer = "안전하지 않은 내용이 포함될 가능성이 있어 답변을 수정했어요. 다른 방식으로 질문을 시도해 주세요.";
            }
            return new AskResponse(answer, List.of());
        }
    }

    /* ---------- 프롬프트 ---------- */

    private String systemPromptWithContext(String studentProfile) {
        // 초1·초2 대상 특화
        return """
                너는 한국어로 답변하는 초등학생 1학년과 2학년 학습 도우미야.
                말투는 부드럽고 친절한 존댓말을 사용하고, 어려운 단어는 쉬운 말로 풀어서 설명해.
                문장은 짧고, 핵심을 불릿으로 정리하고, 아주 간단한 예시(생활 속 비유)를 1개 정도 포함해.
                먼저 '컨텍스트'에서 근거를 찾아 답하고, 부족하면 일반 교과 상식으로 보충하되 추측은 하지 않아.
                컨텍스트를 사용했다면 마지막에 '출처 요약: 문서 1, 3'처럼 사용한 문서 번호만 적어.
                
                [학생 프로필]
                %s
                """.formatted(studentProfile == null || studentProfile.isBlank() ? "제공된 정보 없음" : studentProfile);
    }

    private String systemPromptFallback(String studentProfile) {
        return """
                너는 한국어로 답변하는 초등학생 1학년과 2학년 학습 도우미야.
                문장은 짧게, 쉬운 어휘로 설명하고, 핵심은 불릿으로 정리해.
                개념을 먼저 간단히 말하고, 생활 속 예시 1개를 들어 이해를 도와줘.
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

    // AskRequest.context 로 받은 정보를 간단한 텍스트로 풀어 프롬프트에 넣음
    private String buildStudentProfile(Map<String, Object> ctx) {
        if (ctx == null || ctx.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        // 자유 키: grade, readingLevel, interests, recentScores, strengths, weaknesses, examHistory 등
        appendIfPresent(sb, ctx, "memberNo", "회원번호");
        appendIfPresent(sb, ctx, "grade", "학년");
        appendIfPresent(sb, ctx, "readingLevel", "읽기 수준");
        appendIfPresent(sb, ctx, "strengths", "학습 강점");
        appendIfPresent(sb, ctx, "weaknesses", "보완 필요");
        appendIfPresent(sb, ctx, "interests", "관심사");
        Object scores = ctx.get("recentScores");
        if (scores != null) {
            sb.append("- 최근 점수: ").append(scores).append("\n");
        }
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
