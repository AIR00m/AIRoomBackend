package com.airoom.airoom.aichat.model.repository;

import com.airoom.airoom.aichat.entity.AiChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatRoomRepository extends JpaRepository<AiChatRoom, Long> {
}
