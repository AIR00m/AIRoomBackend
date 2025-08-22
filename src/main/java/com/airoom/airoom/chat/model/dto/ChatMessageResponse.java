package com.airoom.airoom.chat.model.dto;

import com.airoom.airoom.common.value.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private Long roomId;
    private Long messageId;
    private String content;
    private MemberRole writerRole;
    private String senderName;
    private LocalDateTime sentAt;
}
