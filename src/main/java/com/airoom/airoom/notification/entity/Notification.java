package com.airoom.airoom.notification.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.notification.entity.value.NotificationType;
import com.airoom.airoom.notification.entity.value.ReadType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETE_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql="UPDATE notification SET deleted_at = NOW() WHERE notification_no = ?")
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationNo;

    private String notificationUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private ReadType notificationReadType=ReadType.N;

    @ManyToOne
    @JoinColumn(name = "member_no")
    private Member member;

    //디폴트값설정
    @PrePersist
    public void prePersist() {
        if(notificationReadType==null){
            notificationReadType=ReadType.N;
        }
    }
}
