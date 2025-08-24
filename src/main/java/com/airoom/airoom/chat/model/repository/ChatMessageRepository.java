package com.airoom.airoom.chat.model.repository;

import com.airoom.airoom.chat.entity.ChatMessage;
import com.airoom.airoom.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 무한스크롤: 이전 메시지 기준 ↓
    List<ChatMessage> findByChatRoomAndCmNoLessThanOrderByCmNoDesc(
            ChatRoom room, Long beforeId, Pageable pageable);

    // 첫 로드(커서 없을 때)
    List<ChatMessage> findByChatRoomOrderByCmNoDesc(
            ChatRoom room, Pageable pageable);
}
