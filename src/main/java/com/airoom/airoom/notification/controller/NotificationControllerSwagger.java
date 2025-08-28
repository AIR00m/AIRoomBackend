package com.airoom.airoom.notification.controller;

import com.airoom.airoom.notification.model.dto.MarkAsReadRequestDto;
import com.airoom.airoom.notification.model.dto.NotificationListRequestDto;
import com.airoom.airoom.notification.model.dto.NotificationListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "알림 기능 API", description = "알림 기능 API")
public interface NotificationControllerSwagger {

    @Operation(
            summary = "알림 리스트 조회 API",
            description = "해당 학생에게 보내진 알림을 출력합니다.")
    public ResponseEntity<NotificationListResponseDto> getNotificationList(
            @RequestBody NotificationListRequestDto request);

    @Operation(
            summary = "알림 읽음 처리 API",
            description = "학생이 알림을 읽으면 읽음 처리를 합니다.")
    public ResponseEntity<Void> markAsRead(@RequestBody MarkAsReadRequestDto markAsReadRequestDto);

    @Operation(
            summary = "미확인 알림 수 카운트 API",
            description = "학생에게 보내진 알림중에 읽지 않은 알림 수를 조회합니다.")
    @GetMapping("/unreadCount")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(@RequestParam("memberId") String memberId);
    }

