package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.entity.AiChatMessage;
import com.airoom.airoom.aichat.entity.AiChatRoom;
import com.airoom.airoom.aichat.entity.value.MessageType;
import com.airoom.airoom.aichat.model.dto.AskRequest;
import com.airoom.airoom.aichat.model.dto.AskResponse;
import com.airoom.airoom.aichat.model.repository.AiChatMessageRepository;
import com.airoom.airoom.aichat.model.repository.AiChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiChatService {
    private final AiChatRoomRepository roomRepo;
    private final AiChatMessageRepository msgRepo;
    private final RagService ragService;

    @Transactional
    public AskResponse ask(Long roomId, String question) {
        AiChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

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

        // 룸 메타 업데이트
        roomRepo.save(AiChatRoom.builder()
                .acrNo(room.getAcrNo())
                .lastQuestion(question)
                .lastQuestionTime(LocalDateTime.now())
                .member(room.getMember())
                .build());

        return resp;
    }
}
