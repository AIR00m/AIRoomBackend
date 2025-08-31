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
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    /** 인스턴스마다 ‘항상 같은’ 이름(로컬/단일 인스턴스 기준) — 재시작 시 UUID 쓰지 않음(PEL 고아 방지) */
    private String consumerName;

    /** 컨테이너를 필드로 보관 (GC/라이프사이클 이슈 방지) */
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.consumerName = resolveConsumerName();
        log.info("Redis Stream Listener 초기화 시작, consumerName={}", consumerName);

        // ✅ 스트림 없으면 먼저 생성 → 그 다음 그룹 생성 (초기 부팅 race/NOGROUP 방지)
        createStreamAndGroupIfNeeded();

        // ✅ 에러 핸들러 추가 (컨테이너 내부 예외가 묻히는 문제 방지)
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.<String, MapRecord<String, String, String>>builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .batchSize(10)
                        .errorHandler(e -> log.error("❌ Redis stream listener error", e))
                        .build();

        container = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);

        final String stream = RedisStreamKey.NOTIFICATION_STREAM.getKey();
        final String group  = RedisStreamKey.CONSUMER_GROUP.getKey();

        container.receive(
                Consumer.from(group, consumerName),
                StreamOffset.create(stream, ReadOffset.lastConsumed()),
                message -> {
                    final String messageId = message.getId().getValue();
                    try {
                        String payload = message.getValue().get("payload");
                        log.info("Redis Stream 수신 id={}, payload={}", messageId, payload);

                        NotificationEventDto dto = objectMapper.readValue(payload, NotificationEventDto.class);

                        // 실제 알림 처리 (DB 저장 + SSE 전송)
                        notificationService.sendNotification(dto);

                        // ✅ ACK: 반드시 ‘스트림 키 + 그룹 + 레코드ID’로 명시(잘못된 파라미터 순서/오버로드 혼동 방지)
                        redisTemplate.opsForStream()
                                .acknowledge(stream, group, message.getId());
                        log.debug("메시지 ACK 완료: {}", messageId);

                    } catch (Exception e) {
                        // 실패 시 ACK 하지 않음 → PEL에 남아 재처리 가능
                        log.error("메시지 처리 실패(id={}): {}", messageId, e.getMessage(), e);
                    }
                }
        );

        container.start();
        log.info("Redis Stream Listener 시작 완료 (Group: {}, Consumer: {})", group, consumerName);
    }

    /** 로컬/단일 인스턴스 기준: 매 재시작마다 동일한 이름 사용(PEL 고아 방지) */
    private String resolveConsumerName() {
        try {
            final String host = InetAddress.getLocalHost().getHostName();
            return "noti-" + host;
        } catch (Exception e) {
            return "noti-local";
        }
    }

    /**
     * ✅ 스트림을 먼저 ‘존재 보장’한 뒤, 그룹을 생성
     * - 스트림이 없는데 바로 createGroup() 호출하면 ‘no such key’로 실패하고,
     *   이어지는 receive()가 NOGROUP 등으로 조용히 멎을 수 있음.
     */
    private void createStreamAndGroupIfNeeded() {
        final String stream = RedisStreamKey.NOTIFICATION_STREAM.getKey();
        final String group  = RedisStreamKey.CONSUMER_GROUP.getKey();

        // 1) 스트림 보장 (부트스트랩 XADD)
        Boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(stream));
        if (!exists) {
            MapRecord<String, String, String> bootstrap =
                    StreamRecords.newRecord()
                            .ofMap(Map.of("bootstrap", "1"))
                            .withStreamKey(stream);

            redisTemplate.opsForStream().add(bootstrap);
            // 필요시 길이 1로 트림(부트스트랩 레코드만 유지)
            redisTemplate.opsForStream().trim(stream, 1);

            log.info("ℹ️ Stream '{}' not found → created bootstrap entry", stream);
        }

        // 2) 그룹 생성 (이미 있으면 BUSYGROUP)
        try {
            redisTemplate.opsForStream().createGroup(stream, ReadOffset.latest(), group);
            log.info("✅ Consumer Group '{}' 생성 완료", group);
        } catch (RedisSystemException ex) {
            if (ex.getCause() instanceof RedisBusyException) {
                log.info("ℹ️ Consumer Group '{}' 이미 존재함", group);
            } else {
                // 여기서 예외를 삼키면 receive()가 NOGROUP로 멎을 수 있으니 그대로 올림
                throw ex;
            }
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
