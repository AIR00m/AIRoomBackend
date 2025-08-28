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
public class UnreadRequest {
    private Long classroomMemberNo;
    private MemberRole memberRole;
}
