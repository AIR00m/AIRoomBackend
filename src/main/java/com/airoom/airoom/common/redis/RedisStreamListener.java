package com.airoom.airoom.common.redis;

import com.airoom.airoom.notification.model.dto.NotificationEventDto;
import com.airoom.airoom.notification.model.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @EventListener(ApplicationReadyEvent.class) // 애플리케이션 기동 완료 후 실행
    public void init() {
        log.info("Redis Stream Listener 초기화 시작");

        // Consumer Group 생성 (없을 경우에만)
        createConsumerGroupIfNotExists();

        // Listener 옵션 설정
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(2)) // 2초마다 폴링
                        .batchSize(10)                      // 한번에 최대 10개 읽기
                        .build();

        // Listener Container 생성
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);

        // ✅ 수동 ACK 기반 구독
        container.receive(
                Consumer.from(
                        RedisStreamKey.CONSUMER_GROUP.getKey(),
                        RedisStreamKey.CONSUMER_NAME.getKey()
                ),
                StreamOffset.create(
                        RedisStreamKey.NOTIFICATION_STREAM.getKey(),
                        ReadOffset.lastConsumed()
                ),
                message -> {
                    try {
                        String payload = message.getValue().get("payload");
                        log.info("Redis Stream 수신 payload: {}", payload);

                        NotificationEventDto dto = objectMapper.readValue(payload, NotificationEventDto.class);
                        notificationService.sendNotification(dto);

                        // ✅ 처리 성공 시 ACK
                        redisTemplate.opsForStream().acknowledge(
                                RedisStreamKey.CONSUMER_GROUP.getKey(),
                                message
                        );
                        log.debug("메시지 ACK 완료: {}", message.getId());

                    } catch (Exception e) {
                        log.error("메시지 처리 실패: {}", e.getMessage(), e);
                        // ACK 하지 않음 → 이후 XPENDING 상태로 남아 재처리 가능
                    }
                }
        );

        container.start();
        log.info("Redis Stream Listener 시작 완료 (Group: {}, Consumer: {})",
                RedisStreamKey.CONSUMER_GROUP.getKey(),
                RedisStreamKey.CONSUMER_NAME.getKey());
    }

    /**
     * Consumer Group이 존재하지 않으면 생성
     */
    private void createConsumerGroupIfNotExists() {
        try {
            redisTemplate.opsForStream().createGroup(
                    RedisStreamKey.NOTIFICATION_STREAM.getKey(),
                    ReadOffset.latest(),
                    RedisStreamKey.CONSUMER_GROUP.getKey()
            );
            log.info("✅ Consumer Group '{}' 생성 완료", RedisStreamKey.CONSUMER_GROUP.getKey());
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.info("⚠️ Consumer Group '{}' 이미 존재", RedisStreamKey.CONSUMER_GROUP.getKey());
            } else {
                log.error("❌ Consumer Group '{}' 생성 실패: {}", RedisStreamKey.CONSUMER_GROUP.getKey(), e.getMessage(), e);
            }
        }
    }
}