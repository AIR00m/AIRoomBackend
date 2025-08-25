package com.airoom.airoom.common.redis;

import com.airoom.airoom.notification.model.dto.NotificationEventDto;
import com.airoom.airoom.notification.model.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.airoom.airoom.common.redis.RedisStreamKey.ASSIGNMENT_PUB;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamListener {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @PostConstruct
    //애플리케이션 시작 시 @PostConstruct로 리스너 초기화
    //객체 생성하고 의존성 주입이 끝나고 나서 한번만 호출
    //Redis 스트림 구독 리스너를 애플리케이션 시작 시점에 자동으로 실행하되, 의존성 주입이 끝난 안전한 시점에서 실행하기 위해서
    public void createAssignment() {
        //ListenerContainer의 옵션 설정
        //
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>>
                container = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);
        // 첫번째는 redis랑 연결을 해야하니, redisConnectionFactory를 넣어주고
        // 두번째는 우리가 어떤 메시지를 수신할 것인지, 어떤 주기로 폴링할 것인지에 대한 옵션을 만들었기 때문에 옵션도 주입한다.

        container.receive(
//                (과거 메시지 무시)
                StreamOffset.create(ASSIGNMENT_PUB.getKey(), ReadOffset.latest()),
                message -> {
                    try {
                        String payload = message.getValue().get("payload");
                        log.info("payload : {}", payload);
                        NotificationEventDto notificationEventDto = objectMapper.readValue(payload, NotificationEventDto.class);
                        notificationService.assignmentNotification(notificationEventDto);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
        );

        container.start();
    }
}
