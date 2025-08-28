// src/main/java/.../aichat/controller/AiChatRoomController.java
package com.airoom.airoom.aichat.controller;

import com.airoom.airoom.aichat.entity.AiChatRoom;
import com.airoom.airoom.aichat.model.dto.ChatDto.*;
import com.airoom.airoom.aichat.model.repository.AiChatRoomRepository;
import com.airoom.airoom.aichat.model.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aichat/rooms")
@RequiredArgsConstructor
public class AiChatRoomController {

    private final AiChatRoomRepository roomRepo;
    private final AiChatService aiChatService;

    // 방 생성 (memberNo는 프론트에서 전달; 추후 JWT로 교체 가능)
    @PostMapping
    public ResponseEntity<RoomRes> create(@RequestBody RoomCreateReq req){
        // 최소 필드만 사용. member 세팅은 추후 JWT ↔ Member 조회로 강화 가능.
        AiChatRoom room = AiChatRoom.builder()
                .member(null) // TODO: memberRepo.findById(req.memberNo)로 연결 가능하면 세팅
                .lastQuestion(null)
                .lastQuestionTime(null)
                .build();
        roomRepo.save(room);
        return ResponseEntity.ok(RoomRes.from(room));
    }

    // 내 방 목록
    @GetMapping
    public ResponseEntity<List<RoomRes>> list(@RequestParam Long memberNo){
        // 지금은 member 연동 전이므로 일단 전체 최신 N개 리턴(데모용).
        // 실제론 memberNo 기준 where member_no = ? 필요.
        var rooms = roomRepo.findAll(PageRequest.of(0, 50)).stream()
                .map(RoomRes::from).toList();
        return ResponseEntity.ok(rooms);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(@PathVariable Long roomId){
        aiChatService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

}
