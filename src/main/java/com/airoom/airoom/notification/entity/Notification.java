package com.airoom.airoom.notification.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.notification.model.dto.NotificationType;
import com.airoom.airoom.notification.model.dto.ReadType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETE_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql="UPDATE NOTIFICATION SET DELETE_AT = NOW() WHERE NOTIFICATION_NO = ?")
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationNo;

    @Column(nullable = false)
    private String notificationTitle;

    @Column(nullable = false)
    private String notificationContent;

//    @ManyToOne
//    private MEMBER recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReadType notificationReadType=ReadType.N;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    private String notificationUrl;

    //디폴트값설정
    @PrePersist
    public void prePersist() {
        if(notificationReadType==null){
            notificationReadType=ReadType.N;
        }
    }
}
