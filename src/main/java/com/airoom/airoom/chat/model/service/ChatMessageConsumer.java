package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.model.dto.ChatMessageRequest;
import com.airoom.airoom.chat.model.dto.ChatMessageResponse;
import com.airoom.airoom.common.value.MemberRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {
    private final RedisConnectionFactory connectionFactory;
    private final ChatMessageService messageService;
    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String STREAM = "chat:stream";
    private static final String GROUP  = "chat-group";

    /*@PostConstruct
    public void consume() {

        // 옵션 세팅
       StreamMessageListenerContainer.StreamMessageListenerContainerOptions<Object, MapRecord<Object, Object, Object>> options =
               StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .pollTimeout(Duration.ofSeconds(2))   // 블로킹 시간
                .batchSize(20)                        // 한번에 읽을 개수
                .targetType(ObjectRecord.class).serializer(RedisSerializer.json())
                .build();

        StreamMessageListenerContainer<Object, MapRecord<Object, Object, Object>> container = StreamMessageListenerContainer.create(connectionFactory, options);

        // 수동 ACK 모드
        container.receive(
                Consumer.from("chat-group", "consumer-1"),
                StreamOffset.create("chat:stream", ReadOffset.lastConsumed()),
                record -> {
                    //Map<String, String> data = (Map<String, String>) record.getValue();

                    try {
                        String json =(String) record.getValue().get("payload");
                        // === 트랜잭션 안에서 처리 ===
                        *//*Long crNo = Long.parseLong(data.get("crNo"));
                        String content = data.get("content");
                        MemberRole writerRole = MemberRole.valueOf(data.get("writerRole"));
                        LocalDateTime sentAt = LocalDateTime.parse(data.get("sentAt"));*//*
                        ChatMessageRequest req = objectMapper.readValue(json, ChatMessageRequest.class);

                        // DB 저장 (트랜잭션 적용됨)
                        Long msgId = messageService.saveMessage(req.getCrNo(), req.getContent(), req.getWriterRole(), req.getSentAt());

                        // 브로드캐스트
                        ChatMessageResponse dto = ChatMessageResponse.builder()
                                .crNo(req.getCrNo())
                                .messageId(msgId)
                                .content(req.getContent())
                                .writerRole(req.getWriterRole())
                                .sentAt(req.getSentAt())
                                .build();
                        messagingTemplate.convertAndSend("/topic/chat/" + req.getCrNo(), dto);

                        // 성공하면 ACK
                        redis.opsForStream().acknowledge(STREAM, GROUP, record.getId());

                    } catch (Exception e) {
                        // 실패 시 ACK 안 함 → PEL(Pending) 에 남음
                        // 이후 XAUTOCLAIM 같은 방식으로 재처리 가능
                        System.err.println("Consume error: " + e.getMessage());
                    }
                });

        container.start();
    }*/

    @PostConstruct
    public void consume() {
        new Thread(()-> {
            while (true) {
                try {
                    List<MapRecord<String, Object, Object>> records = redis.opsForStream().read(
                            Consumer.from(GROUP, "consumer-1"),
                            StreamReadOptions.empty().count(50).block(Duration.ofSeconds(2)),
                            StreamOffset.create(STREAM, ReadOffset.lastConsumed())
                    );
                    if (records == null || records.isEmpty()) {
                        continue;
                    }
                    for (MapRecord<String, Object, Object> record : records) {
                        try {
                            // 2. ObjectRecord 방식 → Jackson으로 DTO 역직렬화
                            //    (RedisSerializer.json()을 안 쓰고 직접 ObjectMapper 사용)
                            ChatMessageRequest req =
                                    objectMapper.convertValue(record.getValue(), ChatMessageRequest.class);
                            System.out.println("RECORD >>> " + record);
                            System.out.println("VALUE >>> " + record.getValue());

                            // 3. DB 저장
                            Long msgId = messageService.saveMessage(
                                    req.getCrNo(),
                                    req.getContent(),
                                    req.getWriterRole(),
                                    req.getSentAt()
                            );

                            // 4. 브로드캐스트
                            ChatMessageResponse dto = ChatMessageResponse.builder()
                                    .crNo(req.getCrNo())
                                    .messageId(msgId)
                                    .content(req.getContent())
                                    .writerRole(req.getWriterRole())
                                    .sentAt(req.getSentAt())
                                    .build();
                            messagingTemplate.convertAndSend("/topic/chat/" + req.getCrNo(), dto);

                            // 5. ACK (정상 처리 시)
                            redis.opsForStream().acknowledge(STREAM, GROUP, record.getId());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
