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
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql="UPDATE notification SET deleted_at = NOW() WHERE notification_no = ?")
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationNo;

    private String notificationUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
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
// ==================== 도메인 메서드 추가 ====================

    /**
     * 알림을 읽음 상태로 변경
     */
    public void markAsRead() {
        if (this.notificationReadType == ReadType.N) {
            this.notificationReadType = ReadType.Y;
        }
    }

    /**
     * 알림을 읽지 않음 상태로 변경
     */
    public void markAsUnread() {
        if (this.notificationReadType == ReadType.Y) {
            this.notificationReadType = ReadType.N;
        }
    }

    /**
     * 읽음 상태인지 확인
     */
    public boolean isRead() {
        return this.notificationReadType == ReadType.Y;
    }

    /**
     * 읽지 않음 상태인지 확인
     */
    public boolean isUnread() {
        return this.notificationReadType == ReadType.N;
    }


}
