package com.airoom.airoom.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponse {
    private Long crNo;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
    private String studentName;
}
