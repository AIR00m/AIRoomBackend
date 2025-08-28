package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.entity.AiChatMessage;
import com.airoom.airoom.aichat.entity.AiChatRoom;
import com.airoom.airoom.aichat.entity.value.MessageType;
import com.airoom.airoom.aichat.model.dto.AskRequest;
import com.airoom.airoom.aichat.model.dto.AskResponse;
import com.airoom.airoom.aichat.model.dto.ChatDto;
import com.airoom.airoom.aichat.model.repository.AiChatMessageRepository;
import com.airoom.airoom.aichat.model.repository.AiChatRoomRepository;
import com.airoom.airoom.common.token.CustomUserDetails;
import com.airoom.airoom.member.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatService {
    private final AiChatRoomRepository roomRepo;
    private final AiChatMessageRepository msgRepo;
    private final RagService ragService;
    private final MemberRepository memberRepo;

    private Long currentMemberNo() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var me = (CustomUserDetails) auth.getPrincipal();
        return me.getMemberNo(); //  CustomUserDetails에 추가해둔 그 필드
    }

    @Transactional
    public AskResponse ask(Long roomId, String question) {
        AiChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

        //  소유권 검증
        if (!room.getMember().getMemberNo().equals(currentMemberNo())) {
            throw new AccessDeniedException("not your room");
        }

        // Q 저장
        msgRepo.save(AiChatMessage.builder()
                .aiChatRoom(room)
                .acmContent(question)
                .acmType(MessageType.QUESTION)
                .build());

        // 동기 RAG 호출
        AskRequest req = new AskRequest();
        req.setRoomId(String.valueOf(roomId));
        req.setMessage(question);
        AskResponse resp = ragService.ask(req);

        // A 저장
        msgRepo.save(AiChatMessage.builder()
                .aiChatRoom(room)
                .acmContent(resp.getAnswer())
                .acmType(MessageType.ANSWER)
                .build());

        // 메타 업데이트: 엔티티 변경감지
        room.setLastQuestion(question);
        room.setLastQuestionTime(LocalDateTime.now());

        return resp;
    }

    @Transactional(readOnly = true)
    public List<ChatDto.MsgRes> getMessages(Long roomId, Long beforeId, int limit) {
        //  소유권 검증
        roomRepo.findById(roomId).ifPresent(r -> {
            if (!r.getMember().getMemberNo().equals(currentMemberNo())) {
                throw new AccessDeniedException("not your room");
            }
        });

        int size = Math.max(1, Math.min(limit, 100));
        Pageable p = PageRequest.of(0, size);

        List<AiChatMessage> list = (beforeId == null)
                ? msgRepo.findByAiChatRoom_AcrNoOrderByAcmNoDesc(roomId, p)
                : msgRepo.findByAiChatRoom_AcrNoAndAcmNoLessThanOrderByAcmNoDesc(roomId, beforeId, p);

        return list.stream().map(ChatDto.MsgRes::from).toList();
    }

    @Transactional
    public void deleteRoom(Long roomId){
        AiChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));

        //  소유권 검증
        if (!room.getMember().getMemberNo().equals(currentMemberNo())) {
            throw new AccessDeniedException("not your room");
        }

        // 1) 메시지 soft delete
        msgRepo.softDeleteByRoom(roomId);
        // 2) 방 soft delete
        roomRepo.deleteById(roomId);
    }
}

