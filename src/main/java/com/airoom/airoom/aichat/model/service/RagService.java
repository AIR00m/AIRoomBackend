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

            [학생 프로필 사용 지침]
            - 학생이 자신의 학교/반을 묻는 등 개인정보형 질문을 하면, 아래 '학생 프로필'에 학교/반 정보가 있는 경우
              한 문장으로 자연스럽게 알려줘(예: "서울초등학교 1반이에요.").
            - '최근 시험/학습 요약' 관련 항목(최근 시험, 학습일수/시간/정답률 등)이 보이지 않으면
              최근 전학/입학 초기일 수 있으니, 불안감을 줄이는 격려형 톤을 조금 더 사용하고,
              일반적인 조언을 간단히 덧붙여줘.
            
            [중요 규칙]
            - 학생 프로필은 '학생 자신의 개인정보를 묻는 질문(나/내/우리 반/우리 학교 등 자칭 표현 포함)'에만 사용해.
            - 직업/상식/교과 등 일반 질문에는 학생 프로필 내용을 **절대 답변에 사용하지 마**.
            - 모호한 후속 질문(예: "어디에서 일해요?")은 직전 질문의 **주제(직업)**를 이어서 해석해.
            - 컨텍스트에 문장이 있으면 **그 표현을 우선적으로** 사용해 답해. (가능하면 문구를 유지)
            
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

            [학생 프로필 사용 지침]
            - 학생이 자신의 학교/반을 묻는 등 개인정보형 질문을 하면, 아래 '학생 프로필'에 학교/반 정보가 있는 경우
              한 문장으로 자연스럽게 알려줘(예: "서울초등학교 1반이에요.").
            - '최근 시험/학습 요약' 관련 항목(최근 시험, 학습일수/시간/정답률 등)이 보이지 않으면
              최근 전학/입학 초기일 수 있으니, 불안감을 줄이는 격려형 톤을 조금 더 사용하고,
              일반적인 조언을 간단히 덧붙여줘.
            
            [중요 규칙]
            - 학생 프로필은 '학생 자신의 개인정보를 묻는 질문(나/내/우리 반/우리 학교 등 자칭 표현 포함)'에만 사용해.
            - 직업/상식/교과 등 일반 질문에는 학생 프로필 내용을 **절대 답변에 사용하지 마**.
            - 모호한 후속 질문(예: "어디에서 일해요?")은 직전 질문의 **주제(직업)**를 이어서 해석해.
            - 컨텍스트에 문장이 있으면 **그 표현을 우선적으로** 사용해 답해. (가능하면 문구를 유지)
            

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
            String type = lower(p.get("type"));
            ctx.append("### 문서 ").append(i + 1).append("\n");

            // 1) QA 문서
            if ("qa".equals(type)) {
                String job = firstNonBlank(p.get("job"), p.get("title"));
                appendLine(ctx, "- 직업: ", job);
                appendLine(ctx, "- 질문: ", p.get("question"));
                appendLine(ctx, "- 답변: ", p.get("answer"));
                ctx.append("\n");
                continue;
            }

            // 2) 직업 설명 문서 (신규/레거시)
            if ("description".equals(type) || "job_description".equals(type)) {
                String job = firstNonBlank(p.get("job"), p.get("title"));
                appendLine(ctx, "- 직업: ", job);

                boolean hasSeparatedFields =
                        p.containsKey("definition") || p.containsKey("what_1") || p.containsKey("what_2")
                                || p.containsKey("what_3") || p.containsKey("where")   || p.containsKey("who")
                                || p.containsKey("fun_fact");

                if (hasSeparatedFields) {
                    appendLine(ctx, "- 한 줄 정의: ", p.get("definition"));
                    appendLine(ctx, "- 하는 일1: ", p.get("what_1"));
                    appendLine(ctx, "- 하는 일2: ", p.get("what_2"));
                    appendLine(ctx, "- 하는 일3: ", p.get("what_3"));
                    appendLine(ctx, "- 일하는 곳: ", p.get("where"));
                    appendLine(ctx, "- 누가 도움: ", p.get("who"));
                    appendLine(ctx, "- 재미있는 사실: ", p.get("fun_fact"));
                    ctx.append("\n");
                } else {
                    // 레거시: body 에 모든 문구가 합쳐져 있음
                    appendLine(ctx, "", p.get("body"));
                    ctx.append("\n");
                }
                continue;
            }

            // 3) 미정의 타입: 안전 폴백
            ctx.append(String.valueOf(p)).append("\n\n");
        }
        return ctx.toString();
    }

    // --- 아래 3개 유틸을 RagService 클래스 안에 추가 ---
    private static void appendLine(StringBuilder sb, String label, Object v) {
        if (v == null) return;
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return;
        sb.append(label).append(s).append("\n");
    }
    private static String lower(Object o) {
        return (o == null) ? "" : String.valueOf(o).toLowerCase();
    }
    private static String firstNonBlank(Object... xs) {
        for (Object x : xs) {
            if (x == null) continue;
            String s = String.valueOf(x).trim();
            if (!s.isEmpty()) return s;
        }
        return "";
    }

    private String buildStudentProfile(Map<String, Object> ctx) {
        if (ctx == null || ctx.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();

        // --- Member/Profile ---
        appendIfPresent(sb, ctx, "memberNo", "회원번호");
        appendIfPresent(sb, ctx, "name", "이름");
        appendIfPresent(sb, ctx, "gender", "성별");
        appendIfPresent(sb, ctx, "school", "학교");
        appendIfPresent(sb, ctx, "grade", "학년");
        appendIfPresent(sb, ctx, "class", "반");

        // --- Classroom ---
        appendIfPresent(sb, ctx, "classroomNo", "클래스 번호");
        appendIfPresent(sb, ctx, "classroomGrade", "클래스 학년");

        // --- Recent Exam ---
        appendIfPresent(sb, ctx, "recentExamScore", "최근 시험 점수");
        appendIfPresent(sb, ctx, "recentExamDone", "최근 시험 완료여부");
        appendIfPresent(sb, ctx, "recentExamEnd", "최근 시험 종료시각");

        // --- Learning Summary ---
        appendIfPresent(sb, ctx, "summaryLearningDays", "학습일수");
        appendIfPresent(sb, ctx, "summaryLearningTimeMs", "총학습시간(ms)");
        appendIfPresent(sb, ctx, "summaryAccuracyRate", "정답률");

        // --- Unit Summary (상위 1~2개) ---
        if (ctx.get("topUnits") != null) {
            sb.append("- topUnits: ").append(ctx.get("topUnits")).append("\n"); // 그대로 출력
        }

        return sb.toString().trim();
    }

    private void appendIfPresent(StringBuilder sb, Map<String, Object> ctx, String key, String label){
        Object v = ctx.get(key);
        if (v != null) sb.append("- ").append(label).append(": ").append(v).append("\n");
    }
}
