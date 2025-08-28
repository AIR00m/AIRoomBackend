package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.aichat.entity.AiChatMessage;
import com.airoom.airoom.aichat.entity.AiChatRoom;
import com.airoom.airoom.aichat.entity.value.MessageType;
import com.airoom.airoom.aichat.model.dto.AskRequest;
import com.airoom.airoom.aichat.model.dto.AskResponse;
import com.airoom.airoom.aichat.model.dto.ChatDto;
import com.airoom.airoom.aichat.model.repository.AiChatMessageRepository;
import com.airoom.airoom.aichat.model.repository.AiChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional(readOnly = true)
    public List<ChatDto.MsgRes> getMessages(Long roomId, Long beforeId, int limit) {
        int size = Math.max(1, Math.min(limit, 100));
        Pageable p = PageRequest.of(0, size);

        List<AiChatMessage> list = (beforeId == null)
                ? msgRepo.findByAiChatRoom_AcrNoOrderByAcmNoDesc(roomId, p)
                : msgRepo.findByAiChatRoom_AcrNoAndAcmNoLessThanOrderByAcmNoDesc(roomId, beforeId, p);

        // 최신→과거로 내려오므로 프론트에서 reverse()해서 보여주기 좋음
        return list.stream().map(ChatDto.MsgRes::from).toList();
    }

    @Transactional
    public void deleteRoom(Long roomId){
        // 1) 메시지 soft delete
        msgRepo.softDeleteByRoom(roomId);
        // 2) 방 soft delete (엔티티에 @SQLDelete 있음)
        roomRepo.deleteById(roomId);
    }

}
