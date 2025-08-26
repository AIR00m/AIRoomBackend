package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.entity.AiChatMessage;
import com.airoom.airoom.aichat.entity.AiChatRoom;
import com.airoom.airoom.aichat.entity.value.MessageType;
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
                .orElseThrow(() -> new IllegalArgumentException("room not found: "+roomId));

        // Q 저장
        AiChatMessage q = AiChatMessage.builder()
                .aiChatRoom(room)
                .acmContent(question)
                .acmType(MessageType.QUESTION)
                .build();
        msgRepo.save(q);

        // RAG
        RagService.Result r = ragService.answer(question).block();

        String answer = r.answer();

        // A 저장
        AiChatMessage a = AiChatMessage.builder()
                .aiChatRoom(room)
                .acmContent(answer)
                .acmType(MessageType.ANSWER)
                .build();
        msgRepo.save(a);

        // 룸 메타 업데이트
        room = AiChatRoom.builder()
                .acrNo(room.getAcrNo())
                .lastQuestion(question)
                .lastQuestionTime(LocalDateTime.now())
                .member(room.getMember())
                .build();
        roomRepo.save(room);

        return AskResponse.builder()
                .answer(answer)
                .sources(r.sources())
                .build();
    }
}
