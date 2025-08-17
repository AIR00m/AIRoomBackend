package com.airoom.airoom.aichat.entity;


import com.airoom.airoom.aichat.entity.value.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE AI_CHAT_MESSAGE SET DELETED_AT = NOW() WHERE ACM_NO = ?")
@AllArgsConstructor
/**
 * AI 채팅 메시지 엔티티
 */
public class AiChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long acmNo; //AI 채팅메시지 고유번호

    @Column(nullable = false)
    private String acmContent; //AI 채팅메시지 내용

    @Column(nullable = false)
    private MessageType acmType; //AI 채팅메시지 타입(QUESTION, ANSWER)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACR_NO")
    private AiChatRoom aiChatRoom; //AI 채팅방
}
