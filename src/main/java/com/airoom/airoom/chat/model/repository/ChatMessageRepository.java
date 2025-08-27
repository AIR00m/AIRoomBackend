package com.airoom.airoom.chat.model.repository;

import com.airoom.airoom.chat.entity.ChatMessage;
import com.airoom.airoom.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 읽음 처리 bulk update
    @Modifying
    @Query("""
              update ChatMessage m set m.readByTeacher = true
              where m.chatRoom.crNo = :roomId and m.cmNo <= :lastId and m.readByTeacher = false
            """)
    int markReadByTeacher(@Param("roomId") Long roomId, @Param("lastId") Long lastId);

    @Modifying
    @Query("""
              update ChatMessage m set m.readByStudent = true
              where m.chatRoom.crNo = :roomId and m.cmNo <= :lastId and m.readByStudent = false
            """)
    int markReadByStudent(@Param("roomId") Long roomId, @Param("lastId") Long lastId);

    // 안 읽은 수(DB 보정용)
    long countByChatRoomCrNoAndReadByTeacherFalse(Long roomId);

    long countByChatRoomCrNoAndReadByStudentFalse(Long roomId);
}
