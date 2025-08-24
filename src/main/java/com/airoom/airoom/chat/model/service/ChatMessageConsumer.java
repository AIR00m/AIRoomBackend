package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.model.dto.ChatMessageResponse;
import com.airoom.airoom.common.value.MemberRole;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {
    private final RedisConnectionFactory connectionFactory;
    private final ChatMessageService messageService;
    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String STREAM = "chat:stream";
    private static final String GROUP  = "chat-group";

    @PostConstruct
    public void consume() {
        // 그룹 생성 (최초 1회)
        try {
            redis.opsForStream().createGroup(STREAM, ReadOffset.latest(), GROUP);
        } catch (Exception ignore) {}

        // 옵션 세팅
        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .pollTimeout(Duration.ofSeconds(2))   // 블로킹 시간
                .batchSize(20)                        // 한번에 읽을 개수
                .targetType(MapRecord.class)
                .build();

        var container = StreamMessageListenerContainer.create(connectionFactory, options);

        // 수동 ACK 모드
        container.receive(
                Consumer.from(GROUP, "consumer-1"),
                StreamOffset.create(STREAM, ReadOffset.lastConsumed()),
                record -> {
                    Map<String, String> data = (Map<String, String>) record.getValue();

                    try {
                        // === 트랜잭션 안에서 처리 ===
                        Long roomId = Long.parseLong(data.get("roomId"));
                        String content = data.get("content");
                        MemberRole writerRole = MemberRole.valueOf(data.get("writerRole"));
                        LocalDateTime sentAt = LocalDateTime.parse(data.get("sentAt"));

                        // DB 저장 (트랜잭션 적용됨)
                        Long msgId = messageService.saveMessage(roomId, content, writerRole, sentAt);

                        // 브로드캐스트
                        var dto = ChatMessageResponse.builder()
                                .roomId(roomId)
                                .messageId(msgId)
                                .content(content)
                                .writerRole(writerRole)
                                .senderName("TODO: memberName")
                                .sentAt(sentAt)
                                .build();
                        messagingTemplate.convertAndSend("/topic/chat/" + roomId, dto);

                        // 성공하면 ACK
                        redis.opsForStream().acknowledge(STREAM, GROUP, record.getId());

                    } catch (Exception e) {
                        // 실패 시 ACK 안 함 → PEL(Pending) 에 남음
                        // 이후 XAUTOCLAIM 같은 방식으로 재처리 가능
                        System.err.println("Consume error: " + e.getMessage());
                    }
                });

        container.start();
    }
}
