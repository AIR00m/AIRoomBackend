package com.airoom.airoom.chat.controller;

import com.airoom.airoom.chat.model.dto.ChatMessageRequest;
import com.airoom.airoom.chat.model.dto.ChatReadRequest;
import com.airoom.airoom.chat.model.service.ChatMessageProducer;
import com.airoom.airoom.chat.model.service.ChatMessageService;
import com.airoom.airoom.chat.model.service.ChatReadService;
import com.airoom.airoom.chat.model.service.ChatRoomService;
import com.airoom.airoom.common.value.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatWsController {
    private final ChatMessageProducer producer;
    private final ChatRoomService roomService;
    private final ChatReadService readService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService messageService;

    @MessageMapping("/chat/send")
    public void send(@Payload ChatMessageRequest request) {
        messageService.saveMessage(request);
    }

    @MessageMapping("/chat/read")
    public void markRead(@Payload ChatReadRequest request) {
        readService.markAsRead(request);
        messagingTemplate.convertAndSend("/topic/chat/read/" + request.getCrNo(), request);
    }
}
