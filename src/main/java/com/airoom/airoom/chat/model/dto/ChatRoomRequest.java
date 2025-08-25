package com.airoom.airoom.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomRequest {
    private Long classroomTeacherNo;
    private Long classroomStudentNo;
    private Long classroomNo;
}
