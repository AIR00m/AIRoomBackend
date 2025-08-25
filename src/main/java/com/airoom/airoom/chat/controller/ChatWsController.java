package com.airoom.airoom.chat.controller;

import com.airoom.airoom.chat.model.dto.ChatMessageRequest;
import com.airoom.airoom.chat.model.service.ChatMessageProducer;
import com.airoom.airoom.chat.model.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatWsController {
    private final ChatMessageProducer producer;
    private final ChatRoomService roomService;

    @MessageMapping("/chat/send")
    public void send(@Payload ChatMessageRequest request) {
        System.out.println("=== 메시지 수신 ===");
        System.out.println("채팅방: " + request.getCrNo());
        System.out.println("내용: " + request.getContent());
        System.out.println("작성자: " + request.getWriterRole());
        request.setSentAt(LocalDateTime.now());
        producer.publish(request);
    }
}
