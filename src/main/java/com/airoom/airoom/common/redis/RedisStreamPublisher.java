package com.airoom.airoom.common.redis;

import com.airoom.airoom.notification.model.dto.NotificationEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamPublisher {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishNotification(NotificationEventDto notification) { // 학생에게 알림 보낼 dto 생성 후 넣기
        try {
            String json = objectMapper.writeValueAsString(notification);//객체를 json문자열로 직렬화

            //ObjectMapper(ofObject) vs MapRecord(ofMap)
            //
            MapRecord<String, String, String> record =
                    StreamRecords.newRecord()
                            .ofMap(Map.of("payload", json))//payload 설정
                            .withStreamKey(RedisStreamKey.NOTIFICATION_STREAM.getKey());//스트림 키 지정

            log.info("알림 메시지 발행 - Type: {}, 대상자 수: {}", notification.notificationType(), notification.targetMemberNos().size());

            redisTemplate.opsForStream().add(record);
            log.info("알림 메시지 발행 성공");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
