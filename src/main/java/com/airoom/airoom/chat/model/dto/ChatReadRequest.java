package com.airoom.airoom.chat.model.dto;

import com.airoom.airoom.common.value.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatReadRequest {
    private Long crNo; //채팅방 번호
    private Long lastReadMessageId; //채팅방 입장했을때의 마지막 메시지 cmNO
    private MemberRole readerRole;
}
