package com.airoom.airoom.notification.model.service;

import com.airoom.airoom.notification.model.dto.NotificationEventDto;
import com.airoom.airoom.notification.model.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void assignmentNotification(NotificationEventDto notificationEventDto) {
        notificationEventDto.targetMemberNos().forEach(targetNo -> {log.info("targetNo : {}", targetNo);});
        // 알림 보내는 로직 sse 실행
    }
}
