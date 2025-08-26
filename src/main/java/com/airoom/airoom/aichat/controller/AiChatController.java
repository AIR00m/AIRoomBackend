package com.airoom.airoom.aichat.controller;

import com.airoom.airoom.aichat.model.dto.AskRequest;
import com.airoom.airoom.aichat.model.dto.AskResponse;
import com.airoom.airoom.aichat.model.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aichat")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService chatService;

    @PostMapping("/rooms/{roomId}/ask")
    public ResponseEntity<AskResponse> ask(
            @PathVariable Long roomId,
            @RequestBody AskRequest req
    ) {
        AskResponse res = chatService.ask(roomId, req.getQuestion());
        return ResponseEntity.ok(res);
    }
}
