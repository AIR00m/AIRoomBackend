package com.airoom.airoom.aichat.model.repository;

import com.airoom.airoom.aichat.entity.AiChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    // 최신순 페이지(초기 로드)
    List<AiChatMessage> findByAiChatRoom_AcrNoOrderByAcmNoDesc(Long acrNo, Pageable pageable);

    // beforeId 보다 작은(더 과거) 메시지 페이징
    List<AiChatMessage> findByAiChatRoom_AcrNoAndAcmNoLessThanOrderByAcmNoDesc(Long acrNo, Long beforeId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
            UPDATE ai_chat_message
               SET deleted_at = NOW()
             WHERE acr_no = :roomId
               AND deleted_at IS NULL
            """,
            nativeQuery = true
    )
    int softDeleteByRoom(@Param("roomId") Long roomId);

}
