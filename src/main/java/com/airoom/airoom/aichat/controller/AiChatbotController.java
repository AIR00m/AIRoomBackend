package com.airoom.airoom.aichat.controller;

import com.airoom.airoom.aichat.entity.AiChatRoom;
import com.airoom.airoom.aichat.model.AiProps;
import com.airoom.airoom.aichat.model.dto.*;
import com.airoom.airoom.aichat.model.repository.AiChatRoomRepository;
import com.airoom.airoom.aichat.model.service.*;
import com.airoom.airoom.common.token.CustomUserDetails;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aichat")
public class AiChatbotController implements AiChatbotControllerSwagger {

    private final RagService ragService;
    private final AiChatService aiChatService;
    private final AiChatRoomRepository roomRepo;
    private final MemberRepository memberRepo;
    private final WebClient openaiWebClient;
    private final WebClient qdrantWebClient;
    private final QdrantClient qdrant;
    private final OpenAiService openai;
    private final StudentContextService scs;
    private final AiProps props;

    public AiChatbotController(
            RagService ragService,
            AiChatService aiChatService,
            AiChatRoomRepository roomRepo,
            MemberRepository memberRepo,
            @Qualifier("openaiWebClient") WebClient openaiWebClient,
            @Qualifier("qdrantWebClient") WebClient qdrantWebClient,
            QdrantClient qdrant,
            OpenAiService openai,
            StudentContextService scs,
            AiProps props
    ) {
        this.ragService = ragService;
        this.aiChatService = aiChatService;
        this.roomRepo = roomRepo;
        this.memberRepo = memberRepo;
        this.openaiWebClient = openaiWebClient;
        this.qdrantWebClient = qdrantWebClient;
        this.qdrant = qdrant;
        this.openai = openai;
        this.scs = scs;
        this.props = props;
    }

    // ===== AiChatController 기능들 =====

    /**
     * AI 채팅 질문
     */
    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody AskRequest req
    ) {
        String message = (req.getMessage() == null) ? "" : req.getMessage().trim();
        if (message.isBlank()) {
            return ResponseEntity.ok(new AskResponse("질문이 비어있어요. 무엇이 궁금한가요?", List.of()));
        }

        // roomId가 있으면: 저장 + 소유권 체크
        if (StringUtils.hasText(req.getRoomId())) {
            Long roomId;
            try { roomId = Long.valueOf(req.getRoomId()); }
            catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(new AskResponse("올바르지 않은 roomId 입니다.", List.of()));
            }

            var room = roomRepo.findById(roomId).orElse(null);
            if (room == null) {
                return ResponseEntity.badRequest().body(new AskResponse("대화방을 찾을 수 없어요.", List.of()));
            }
            if (me == null || me.getMemberNo() == null || !room.getMember().getMemberNo().equals(me.getMemberNo())) {
                return ResponseEntity.status(403).body(new AskResponse("이 대화방에 질문할 권한이 없어요.", List.of()));
            }
            return ResponseEntity.ok(aiChatService.ask(roomId, message));
        }

        // roomId 없으면 RAG만
        return ResponseEntity.ok(ragService.ask(req));
    }

    /**
     * 채팅방 메시지 조회
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatDto.MsgRes>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "30") int limit,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        var room = roomRepo.findById(roomId).orElse(null);
        if (room == null) return ResponseEntity.notFound().build();
        if (me == null || me.getMemberNo() == null || !room.getMember().getMemberNo().equals(me.getMemberNo())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(aiChatService.getMessages(roomId, beforeId, limit));
    }

    // ===== AiChatRoomController 기능들 =====

    /**
     * 채팅방 생성
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatDto.RoomRes> createRoom() {
        var me = me();
        // 회원 프록시만 얻어서 FK 세팅 (쿼리 안 나감)
        Member owner = memberRepo.getReferenceById(me.getMemberNo());
        AiChatRoom room = AiChatRoom.builder()
                .member(owner)
                .lastQuestion(null)
                .lastQuestionTime(null)
                .build();
        roomRepo.save(room);
        return ResponseEntity.ok(ChatDto.RoomRes.from(room));
    }

    /**
     * 채팅방 목록 조회
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatDto.RoomRes>> listRooms() {
        var me = me();
        var rooms = roomRepo
                .findByMember_MemberNoOrderByLastQuestionTimeDesc(me.getMemberNo())
                .stream().map(ChatDto.RoomRes::from).toList();
        return ResponseEntity.ok(rooms);
    }

    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        aiChatService.deleteRoom(roomId); // 소유권 검증은 서비스에서 한 번 더
        return ResponseEntity.noContent().build();
    }

    // ===== AiChatHealthController 기능들 =====

    /**
     * 시스템 상태 체크
     */
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

    /**
     * Qdrant 컬렉션 개수 조회
     */
    @GetMapping("/health/qdrant-count")
    public Map<String, Object> count() {
        return Map.of("collection", props.getQdrant().getCollection(), "count", qdrant.count());
    }

    /**
     * RAG 없이 드라이 검색 (임베딩+검색만)
     */
    @GetMapping("/health/dry-search")
    public List<SourceDto> drySearch(@RequestParam String q,
                                     @RequestParam(defaultValue = "5") int k) {
        var vec = openai.embed(q);
        return qdrant.search(vec, k, 0.2);
    }

    /**
     * 로그인 사용자의 개인 컨텍스트 확인
     */
    @GetMapping("/health/context")
    public Map<String, Object> context(@AuthenticationPrincipal CustomUserDetails me) {
        return scs.build(me.getMemberNo());
    }

    // ===== Helper 메서드 =====

    private CustomUserDetails me() {
        return (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }
}
