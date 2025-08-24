package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.entity.ChatMessage;
import com.airoom.airoom.chat.entity.ChatRoom;
import com.airoom.airoom.chat.model.dto.ChatMessageResponse;
import com.airoom.airoom.chat.model.repository.ChatMessageRepository;
import com.airoom.airoom.chat.model.repository.ChatRoomRepository;
import com.airoom.airoom.common.value.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public Long saveMessage(Long roomId, String content, MemberRole writerRole, LocalDateTime sentAt) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .cmContent(content)
                .cmWriterType(writerRole)
                .build();

        chatMessageRepository.save(message);

        // 채팅방 마지막 메시지 업데이트
        room.setLastMessage(content);
        room.setLastMessageTime(sentAt);

        return message.getCmNo();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> scroll(Long roomId, Long beforeId, int size) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        Pageable p = PageRequest.of(0, size);
        List<ChatMessageResponse> list = (beforeId == null)
                ? chatMessageRepository.findByChatRoomOrderByCmNoDesc(room, p).stream()
                .map(this::buildChatMessageResponse).toList()
                : chatMessageRepository.findByChatRoomAndCmNoLessThanOrderByCmNoDesc(room, beforeId, p).stream()
                .map(this::buildChatMessageResponse).toList();
        // 화면에서 아래→위 순서를 원하면 역순 반환
        Collections.reverse(list);
        return list;
    }

    private ChatMessageResponse buildChatMessageResponse(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .messageId(chatMessage.getCmNo())
                .roomId(chatMessage.getChatRoom().getCrNo())
                .content(chatMessage.getCmContent())
                .sentAt(chatMessage.getCreatedAt())
                .writerRole(chatMessage.getCmWriterType())
                .build();
    }
}
