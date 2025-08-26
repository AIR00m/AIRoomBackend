package com.airoom.airoom.aichat.model.repository;

import com.airoom.airoom.aichat.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {
}
