// src/main/java/.../aichat/model/dto/ChatDtos.java
package com.airoom.airoom.aichat.model.dto;

import com.airoom.airoom.aichat.entity.AiChatMessage;
import com.airoom.airoom.aichat.entity.AiChatRoom;
import lombok.AllArgsConstructor; import lombok.Data; import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ChatDto {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RoomCreateReq { private Long memberNo; private String title; }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RoomRes {
        private Long roomId;
        private String lastQuestion;
        private LocalDateTime lastQuestionTime;
        public static RoomRes from(AiChatRoom r){
            return new RoomRes(r.getAcrNo(), r.getLastQuestion(), r.getLastQuestionTime());
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class MsgRes {
        private Long id; private String sender; private String text;
        private LocalDateTime createdAt;
        public static MsgRes from(AiChatMessage m){
            return new MsgRes(
                    m.getAcmNo(),
                    m.getAcmType().name().toLowerCase(), // question/answer
                    m.getAcmContent(),
                    m.getCreatedAt()
            );
        }
    }
}
