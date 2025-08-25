package com.airoom.airoom.chat.model.dto;

import com.airoom.airoom.common.value.MemberRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageRequest {
    private Long crNo;//채팅방번호
    private String content;
    private MemberRole writerRole;
    private LocalDateTime sentAt;
}
