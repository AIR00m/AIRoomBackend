package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.model.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatMessageProducer {
    private final StringRedisTemplate redisTemplate;
    private static final String STREAM = "chat:stream";

    public void publish(ChatMessageRequest req) {
        Map<String, String> message = new HashMap<>();
        message.put("roomId", req.getRoomId().toString());
        message.put("content", req.getContent());
        message.put("writerRole", req.getWriterRole().name());
        message.put("sentAt", req.getSentAt().toString());

        redisTemplate.opsForStream().add(STREAM, message);
    }

}
