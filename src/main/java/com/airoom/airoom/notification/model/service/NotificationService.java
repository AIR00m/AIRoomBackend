package com.airoom.airoom.notification.model.service;

import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.repository.MemberRepository;
import com.airoom.airoom.notification.entity.Notification;
import com.airoom.airoom.notification.entity.value.ReadType;
import com.airoom.airoom.notification.model.dto.NotificationDto;
import com.airoom.airoom.notification.model.dto.NotificationEventDto;
import com.airoom.airoom.notification.model.dto.NotificationListResponseDto;
import com.airoom.airoom.notification.model.dto.NotificationResponseDto;
import com.airoom.airoom.notification.model.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmitterService emitterService;
    private final MemberRepository memberRepository;

    private Member getMemberByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다: " + memberId));
    }

    /**
     * 알림 목록 조회 (memberId -> memberNo 변환 후 사용)
     */
    public NotificationListResponseDto getNotificationList(String memberId, int offset, int limit) {
        try {
            // memberId로 Member 엔티티 조회
            Member member = getMemberByMemberId(memberId);
            Long memberNo = member.getMemberNo(); // memberNo 추출

            log.info("알림 목록 조회 - memberId: {}, memberNo: {}", memberId, memberNo);

            // 페이징 처리
            Pageable pageable = PageRequest.of(
                    offset / limit,
                    limit,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
            // memberNo로 알림 조회
            Page<Notification> notificationPage = notificationRepository
                    .findByMember_MemberNoOrderByCreatedAtDesc(memberNo, pageable);

            // DTO 변환
            List<NotificationResponseDto> notifications = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

            for (Notification notification : notificationPage.getContent()) {
                NotificationResponseDto dto = new NotificationResponseDto();
                dto.setNotificationNo(notification.getNotificationNo());
                dto.setNotificationType(notification.getNotificationType().name());
                dto.setMessage(notification.getNotificationType().getMessage());
                dto.setRedirectUrl(notification.getNotificationType().getRedirectLocation());
                dto.setIsRead(notification.getNotificationReadType() == ReadType.Y);
                dto.setNotificationUrl(notification.getNotificationUrl());

                // 시간 포맷팅
                LocalDateTime createdAt = notification.getCreatedAt();
                LocalDateTime now = LocalDateTime.now();
                Duration duration = Duration.between(createdAt, now);

                long minutes = duration.toMinutes();
                if (minutes < 1) {
                    dto.setCreatedTime("방금 전");
                } else if (minutes < 60) {
                    dto.setCreatedTime(minutes + "분 전");
                } else {
                    long hours = duration.toHours();
                    if (hours < 24) {
                        dto.setCreatedTime(hours + "시간 전");
                    } else {
                        long days = duration.toDays();
                        if (days < 7) {
                            dto.setCreatedTime(days + "일 전");
                        } else {
                            dto.setCreatedTime(createdAt.format(formatter));
                        }
                    }
                }

                notifications.add(dto);
            }

            return new NotificationListResponseDto(
                    notifications,
                    notificationPage.hasNext(),
                    (int) notificationPage.getTotalElements()
            );

        } catch (Exception e) {
            log.error("알림 목록 조회 중 오류 발생 - memberId: {}", memberId, e);
            throw new RuntimeException("알림 목록 조회 실패", e);
        }
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationNo) {
        try {
            Notification notification = notificationRepository.findById(notificationNo)
                    .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + notificationNo));

            // 읽음 처리
            if (notification.isUnread()) {  // isUnread() 도메인 메서드 사용
                notification.markAsRead();  // markAsRead() 도메인 메서드 사용
                notificationRepository.save(notification);
                log.info("알림 읽음 처리 완료: {}", notificationNo);
            } else {
                log.info("이미 읽음 처리된 알림: {}", notificationNo);
            }

        } catch (Exception e) {
            log.error("알림 읽음 처리 중 오류 발생 - notificationNo: {}", notificationNo, e);
            throw new RuntimeException("알림 읽음 처리 실패", e);
        }
    }



    /**
     * 읽지 않은 알림 개수 조회
     */
    public Map<String, Integer> getUnreadCount(String memberId) {
        try {
            // memberId로 memberNo 조회
            Member member = getMemberByMemberId(memberId);
            Long memberNo = member.getMemberNo();

            int count = notificationRepository
                    .countByMember_MemberNoAndNotificationReadType(memberNo, ReadType.N);

            Map<String, Integer> response = new HashMap<>();
            response.put("unreadCount", count);

            log.info("읽지 않은 알림 개수: {} (memberId: {}, memberNo: {})", count, memberId, memberNo);
            return response;

        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 중 오류 발생 - memberId: {}", memberId, e);
            throw new RuntimeException("읽지 않은 알림 개수 조회 실패", e);
        }
    }


    @Transactional
    public void sendNotification(NotificationEventDto notificationEventDto) {
        log.info("알림 처리 시작 - Type: {},대상자 수: {}",
                notificationEventDto.notificationType(),
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

