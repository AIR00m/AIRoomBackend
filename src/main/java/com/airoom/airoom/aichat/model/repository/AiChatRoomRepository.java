package com.airoom.airoom.aichat.model.repository;

import com.airoom.airoom.aichat.entity.AiChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatRoomRepository extends JpaRepository<AiChatRoom, Long> {
    List<AiChatRoom> findByMember_MemberNoOrderByLastQuestionTimeDesc(Long memberNo);
}
