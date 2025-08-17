package com.airoom.airoom.chat.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.common.value.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE CHAT_MESSAGE SET DELETED_AT = NOW() WHERE CM_NO = ?")
@AllArgsConstructor
/**
 * 채팅 메시지 엔티티
 */
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cmNo; //채팅 메시지 고유번호

    @Column(nullable = false)
    private String cmContent; //채팅 메시지내용

    @Column(nullable = false)
    @Builder.Default
    private Boolean cmIsRead = false; //채팅 읽음 여부

    @Column(nullable = false)
    private MemberRole cmWriterType; //채팅 작성자 타입

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CR_NO")
    private ChatRoom chatRoom; //채팅방

    @PrePersist
    public void prePersist() {
        if (cmIsRead == null) {
            cmIsRead = false;
        }
    }
}
