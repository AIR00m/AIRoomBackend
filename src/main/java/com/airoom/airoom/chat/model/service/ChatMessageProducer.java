package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.model.dto.ChatMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatMessageProducer {
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private static final String STREAM = "chat:stream";

    public void publish(ChatMessageRequest req) {
       /* Map<String, String> message = new HashMap<>();
        message.put("crNo", String.valueOf(req.getCrNo()));
        message.put("content", req.getContent());
        message.put("writerRole", req.getWriterRole().name());
        message.put("sentAt", req.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        redisTemplate.opsForStream().add(STREAM, message);*/

        try {
            //String json = objectMapper.writeValueAsString(req);
            //Map<String,String> message = Map.of("payload",json);
            ObjectRecord<String, ChatMessageRequest> record =
                    StreamRecords.newRecord()
                                    .ofObject(req)
                                            .withStreamKey("chat:stream");

            redisTemplate.opsForStream().add(record);

        } catch (Exception e) {
            throw new RuntimeException("메시지 직렬화 실패" ,e);
        }
    }

}
