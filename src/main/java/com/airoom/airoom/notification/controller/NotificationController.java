package com.airoom.airoom.notification.controller;

import com.airoom.airoom.notification.model.dto.MarkAsReadRequestDto;
import com.airoom.airoom.notification.model.dto.NotificationListRequestDto;
import com.airoom.airoom.notification.model.dto.NotificationListResponseDto;
import com.airoom.airoom.notification.model.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerSwagger{

    private final NotificationService notificationService;

    @Override
    @PostMapping("/list")
    public ResponseEntity<NotificationListResponseDto> getNotificationList(
            @RequestBody NotificationListRequestDto request) {
        NotificationListResponseDto response = notificationService.getNotificationList(request.getMemberId(),request.getOffset(), request.getLimit());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/markAsRead")
    public ResponseEntity<Void> markAsRead(@RequestBody MarkAsReadRequestDto markAsReadRequestDto) {
        notificationService.markAsRead(markAsReadRequestDto.getNotificationNo());
        return ResponseEntity.ok().build();
    }

    // 모두 읽음 처리 API - 주석 처리
//    @PostMapping("/markAllAsRead")
//    public ResponseEntity<Void> markAllAsRead() {
//        notificationService.markAllAsRead();
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("/unreadCount")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(@RequestParam("memberId") String memberId) {
        Map<String, Integer> response = notificationService.getUnreadCount(memberId);
        return ResponseEntity.ok(response);
    }
}
