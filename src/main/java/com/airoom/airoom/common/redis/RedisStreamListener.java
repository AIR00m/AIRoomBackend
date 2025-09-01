package com.airoom.airoom.common.redis;

import com.airoom.airoom.notification.model.dto.NotificationEventDto;
import com.airoom.airoom.notification.model.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisBusyException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    /** 컨슈머 이름을 인스턴스마다 유니크하게 */
    private String consumerName;

    /** 컨테이너를 필드로 보관 (GC/라이프사이클 이슈 방지) */
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            final String host = InetAddress.getLocalHost().getHostName();
            this.consumerName = "noti-" + host + "-" + UUID.randomUUID();
        } catch (Exception e) {
            this.consumerName = "noti-" + UUID.randomUUID();
        }

        log.info("Redis Stream Listener 초기화 시작, consumerName={}", consumerName);

        // Consumer Group 주석
        //createConsumerGroupIfNotExists();

        // Listener 옵션 설정
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.<String, MapRecord<String, String, String>>builder()
                        .pollTimeout(Duration.ofSeconds(2)) // 2초 폴링
                        .batchSize(10)                      // 최대 10개
                        .build();

        // Listener Container 생성 (필드에 보관)
        container = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);

        // 수동 ACK 기반 구독
        container.receive(
                //컨슈머 삭제
                // Consumer.from(RedisStreamKey.CONSUMER_GROUP.getKey(), consumerName),
                StreamOffset.create(RedisStreamKey.NOTIFICATION_STREAM.getKey(), ReadOffset.lastConsumed()),
                message -> {
                    final String messageId = message.getId().getValue();
                    try {
                        String payload = message.getValue().get("payload");
                        log.info("Redis Stream 수신 id={}, payload={}", messageId, payload);

                        NotificationEventDto dto = objectMapper.readValue(payload, NotificationEventDto.class);

                        // 실제 알림 전송 (SSE 등)
                        notificationService.sendNotification(dto);

                        // 처리 성공 시 ACK
                        redisTemplate.opsForStream()
                                .acknowledge(RedisStreamKey.CONSUMER_GROUP.getKey(), message);
                        log.debug("메시지 ACK 완료: {}", messageId);

                    } catch (Exception e) {
                        // 실패 시 ACK 하지 않음 → PEL에 남아 재처리 가능
                        log.error("메시지 처리 실패(id={}): {}", messageId, e.getMessage(), e);
                    }
                }
        );

        container.start();
        log.info("Redis Stream Listener 시작 완료 (Group: {}, Consumer: {})",
                RedisStreamKey.CONSUMER_GROUP.getKey(), consumerName);
    }

    /**
     * Consumer Group이 존재하지 않으면 생성
     */
    private void createConsumerGroupIfNotExists() {
        try {
            // 주의: 스트림이 없으면 RedisBusyException (BUSYGROUP)이 아니라 'no such key'가 날 수 있음.
            // 여기서는 기존 로직 유지: 이미 존재하면 예외를 정상 처리.
            redisTemplate.opsForStream().createGroup(
                    RedisStreamKey.NOTIFICATION_STREAM.getKey(),
                    ReadOffset.latest(),
                    RedisStreamKey.CONSUMER_GROUP.getKey()
            );
            log.info("✅ Consumer Group '{}' 생성 완료", RedisStreamKey.CONSUMER_GROUP.getKey());
        } catch (RedisSystemException ex) {
            if (ex.getCause() instanceof RedisBusyException) {
                log.info("ℹ️ Consumer Group '{}' 이미 존재함", RedisStreamKey.CONSUMER_GROUP.getKey());
            } else {
                log.error("❌ Consumer Group 생성 실패: {}", ex.getMessage(), ex);
            }
        } catch (Exception e) {
            log.error("❌ Consumer Group 생성 중 예외: {}", e.getMessage(), e);
        }
    }

    /** 애플리케이션 종료 시 컨테이너 정상 정지 */
    @PreDestroy
    public void shutdown() {
        if (container != null) {
            try {
                container.stop();
                log.info("Redis Stream Listener 정상 종료");
            } catch (Exception e) {
                log.warn("Redis Stream Listener 종료 중 예외: {}", e.getMessage(), e);
            }
        }
    }
}
