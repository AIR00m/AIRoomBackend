package com.airoom.airoom.aichat.controller;

import com.airoom.airoom.aichat.model.dto.AskRequest;
import com.airoom.airoom.aichat.model.dto.AskResponse;
import com.airoom.airoom.aichat.model.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aichat")
@RequiredArgsConstructor
public class AiChatController {

    private final RagService ragService;

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest req) {
        return ResponseEntity.ok(ragService.ask(req));
    }
}
