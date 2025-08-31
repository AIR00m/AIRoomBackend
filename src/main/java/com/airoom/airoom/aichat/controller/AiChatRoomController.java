//package com.airoom.airoom.aichat.controller;
//
//import com.airoom.airoom.aichat.entity.AiChatRoom;
//import com.airoom.airoom.aichat.model.dto.ChatDto.*;
//import com.airoom.airoom.aichat.model.repository.AiChatRoomRepository;
//import com.airoom.airoom.aichat.model.service.AiChatService;
//import com.airoom.airoom.common.token.CustomUserDetails;
//import com.airoom.airoom.member.entity.Member;
//import com.airoom.airoom.member.model.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//// AiChatRoomController.java
//@RestController
//@RequestMapping("/aichat/rooms")
//@RequiredArgsConstructor
//public class AiChatRoomController {
//
//    private final AiChatRoomRepository roomRepo;
//    private final AiChatService aiChatService;
//    private final MemberRepository memberRepo;
//
//    private CustomUserDetails me() {
//        return (CustomUserDetails) SecurityContextHolder
//                .getContext().getAuthentication().getPrincipal();
//    }
//
//    @PostMapping
//    public ResponseEntity<RoomRes> create() {
//        var me = me();
//        // 회원 프록시만 얻어서 FK 세팅 (쿼리 안 나감)
//        Member owner = memberRepo.getReferenceById(me.getMemberNo());
//        AiChatRoom room = AiChatRoom.builder()
//                .member(owner)
//                .lastQuestion(null)
//                .lastQuestionTime(null)
//                .build();
//        roomRepo.save(room);
//        return ResponseEntity.ok(RoomRes.from(room));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<RoomRes>> list() {
//        var me = me();
//        var rooms = roomRepo
//                .findByMember_MemberNoOrderByLastQuestionTimeDesc(me.getMemberNo())
//                .stream().map(RoomRes::from).toList();
//        return ResponseEntity.ok(rooms);
//    }
//
//    @DeleteMapping("/{roomId}")
//    public ResponseEntity<Void> delete(@PathVariable Long roomId){
//        aiChatService.deleteRoom(roomId); // 소유권 검증은 서비스에서 한 번 더
//        return ResponseEntity.noContent().build();
//    }
//}
//
//
