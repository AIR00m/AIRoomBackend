package com.airoom.airoom.aichat.controller;

import com.airoom.airoom.aichat.model.dto.AskRequest;
import com.airoom.airoom.aichat.model.dto.AskResponse;
import com.airoom.airoom.aichat.model.service.AiChatService;
import com.airoom.airoom.aichat.model.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aichat")
@RequiredArgsConstructor
public class AiChatController {

    private final RagService ragService;
    private final AiChatService aiChatService;

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest req) {
        String message = (req.getMessage() == null) ? "" : req.getMessage().trim();
        if (!StringUtils.hasText(message)) {
            return ResponseEntity.ok(new AskResponse("질문이 비어있어요. 무엇이 궁금한가요?", java.util.List.of()));
        }

        // roomId가 있으면 DB저장 경로(AiChatService), 없으면 RAG만
        if (StringUtils.hasText(req.getRoomId())) {
            try {
                Long roomId = Long.valueOf(req.getRoomId());
                return ResponseEntity.ok(aiChatService.ask(roomId, message));
            } catch (NumberFormatException e) {
                // roomId가 숫자가 아니면 RAG만 수행 (혹은 400 리턴도 가능)
                return ResponseEntity.ok(ragService.ask(req));
            } catch (IllegalArgumentException notFound) {
                // 방이 없을 때도 RAG만 수행하게 폴백 (원하면 404로 바꿔도 됨)
                return ResponseEntity.ok(ragService.ask(req));
            }
        }

        // 기본: RAG만
        return ResponseEntity.ok(ragService.ask(req));
    }

}
