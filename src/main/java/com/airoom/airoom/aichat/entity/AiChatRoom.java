package com.airoom.airoom.aichat.entity;

import com.airoom.airoom.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE AI_CHAT_ROOM SET DELETED_AT = NOW() WHERE ACR_NO = ?")
@AllArgsConstructor
/**
 * AI 채팅방 엔티티
 */
public class AiChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long acrNo; //AI 채팅방 고유번호

    private String lastQuestion; //AI 채팅방 마지막질문

    private LocalDateTime lastQuestionTime; //AI 채팅방 마지막메시지 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO")
    private Member member; //회원
}
