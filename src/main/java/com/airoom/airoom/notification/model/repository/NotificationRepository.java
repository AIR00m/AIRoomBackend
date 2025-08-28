package com.airoom.airoom.notification.model.repository;

import com.airoom.airoom.notification.entity.Notification;
import com.airoom.airoom.notification.entity.value.ReadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {


    /**
     * memberNo로 알림 목록 조회 (페이징)
     */
    Page<Notification> findByMember_MemberNoOrderByCreatedAtDesc(
            Long memberNo, Pageable pageable);

    /**
     * memberNo로 읽지 않은 알림 개수 조회
     */
    int countByMember_MemberNoAndNotificationReadType(
            Long memberNo, ReadType readType);

    /**
     * memberNo로 특정 읽음 상태 알림 조회
     */
    List<Notification> findByMember_MemberNoAndNotificationReadType(
            Long memberNo, ReadType readType);
}
