package com.airoom.airoom.chat.model.repository;

import com.airoom.airoom.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
