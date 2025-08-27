package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.model.dto.ChatMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageProducer {
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private static final String STREAM = "chat:stream";

    public void publish(ChatMessageRequest req) {
        try {
            ObjectRecord<String, ChatMessageRequest> record =
                    StreamRecords.newRecord()
                            .ofObject(req)
                            .withStreamKey("chat:stream");
            redisTemplate.opsForStream().add(record);
        } catch (Exception e) {
            throw new RuntimeException("메시지 직렬화 실패", e);
        }
    }

}
