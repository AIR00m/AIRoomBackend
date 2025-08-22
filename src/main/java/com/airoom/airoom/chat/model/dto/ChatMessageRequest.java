package com.airoom.airoom.chat.model.dto;

import com.airoom.airoom.common.value.MemberRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
    private String content;
    private MemberRole writerRole;
    private LocalDateTime sentAt;
}
