//package com.airoom.airoom.aichat.controller;
//
//import com.airoom.airoom.aichat.model.dto.AskRequest;
//import com.airoom.airoom.aichat.model.dto.AskResponse;
//import com.airoom.airoom.aichat.model.dto.ChatDto;
//import com.airoom.airoom.aichat.model.repository.AiChatRoomRepository;
//import com.airoom.airoom.aichat.model.service.AiChatService;
//import com.airoom.airoom.aichat.model.service.RagService;
//import com.airoom.airoom.common.token.CustomUserDetails;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/aichat")
//@RequiredArgsConstructor
//public class AiChatController {
//
//    private final RagService ragService;
//    private final AiChatService aiChatService;
//    private final AiChatRoomRepository roomRepo;
//
//    @PostMapping("/ask")
//    public ResponseEntity<AskResponse> ask(
//            @AuthenticationPrincipal CustomUserDetails me,
//            @RequestBody AskRequest req
//    ) {
//        String message = (req.getMessage() == null) ? "" : req.getMessage().trim();
//        if (message.isBlank()) {
//            return ResponseEntity.ok(new AskResponse("질문이 비어있어요. 무엇이 궁금한가요?", List.of()));
//        }
//
//        // roomId가 있으면: 저장 + 소유권 체크
//        if (StringUtils.hasText(req.getRoomId())) {
//            Long roomId;
//            try { roomId = Long.valueOf(req.getRoomId()); }
//            catch (NumberFormatException e) {
//                return ResponseEntity.badRequest().body(new AskResponse("올바르지 않은 roomId 입니다.", List.of()));
//            }
//
//            var room = roomRepo.findById(roomId).orElse(null);
//            if (room == null) {
//                return ResponseEntity.badRequest().body(new AskResponse("대화방을 찾을 수 없어요.", List.of()));
//            }
//            if (me == null || me.getMemberNo() == null || !room.getMember().getMemberNo().equals(me.getMemberNo())) {
//                return ResponseEntity.status(403).body(new AskResponse("이 대화방에 질문할 권한이 없어요.", List.of()));
//            }
//            return ResponseEntity.ok(aiChatService.ask(roomId, message));
//        }
//
//        // roomId 없으면 RAG만
//        return ResponseEntity.ok(ragService.ask(req));
//    }
//
//    @GetMapping("/rooms/{roomId}/messages")
//    public ResponseEntity<List<ChatDto.MsgRes>> messages(
//            @PathVariable Long roomId,
//            @RequestParam(required = false) Long beforeId,
//            @RequestParam(defaultValue = "30") int limit,
//            @AuthenticationPrincipal CustomUserDetails me
//    ) {
//        var room = roomRepo.findById(roomId).orElse(null);
//        if (room == null) return ResponseEntity.notFound().build();
//        if (me == null || me.getMemberNo() == null || !room.getMember().getMemberNo().equals(me.getMemberNo())) {
//            return ResponseEntity.status(403).build();
//        }
//        return ResponseEntity.ok(aiChatService.getMessages(roomId, beforeId, limit));
//    }
//}
//
