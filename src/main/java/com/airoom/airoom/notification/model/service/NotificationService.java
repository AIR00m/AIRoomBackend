package com.airoom.airoom.notification.model.service;

import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.notification.entity.Notification;
import com.airoom.airoom.notification.entity.value.ReadType;
import com.airoom.airoom.notification.model.dto.NotificationDto;
import com.airoom.airoom.notification.model.dto.NotificationEventDto;
import com.airoom.airoom.notification.model.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmitterService emitterService;

    @Transactional
    public void sendNotification(NotificationEventDto notificationEventDto) {
        log.info("알림 처리 시작 - Type: {}, URL: {}, 대상자 수: {}",
                notificationEventDto.notificationType(),
                notificationEventDto.notificationUrl(),
                notificationEventDto.targetMemberNos().size());

        // 각 대상자별로 개별 알림 생성
        notificationEventDto.targetMemberNos().forEach(memberNo -> {
            try{
                // 1. Notification Entity 생성 및 저장
                Notification notification = createNotificationEntity(memberNo, notificationEventDto);
                Notification savedNotification = notificationRepository.save(notification);
                //2.DTO 생성
                NotificationDto notificationDto = NotificationDto.fromEntity(savedNotification);

                //3. SSE 전송
                emitterService.sendNotificationToMember(memberNo, notificationDto);

                log.info("알림 전송 완료 memberMo {}, type {}", memberNo, notificationEventDto.notificationType());
            }catch(Exception e){
                log.error("알림처리 실패 memberMo {}, error {}", memberNo,e.getMessage(),e);
            }
        });

        log.info("알림 처리 완료");
    }



    private Notification createNotificationEntity(Long memberNo, NotificationEventDto eventDto) {
        Member member = Member.builder().memberNo(memberNo).build(); // 프록시 객체

        return Notification.builder()
                .notificationUrl(eventDto.notificationUrl())
                .notificationType(eventDto.notificationType())
                .member(member)
                .notificationReadType(ReadType.N) // 미읽음 상태
                .build();
    }
}

