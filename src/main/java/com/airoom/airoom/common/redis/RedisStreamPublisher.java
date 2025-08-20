package com.airoom.airoom.common.redis;

import com.airoom.airoom.common.redis.model.dto.AssignmentCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.airoom.airoom.common.redis.RedisStreamKey.ASSIGNMENT_PUB;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamPublisher {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void createAssignment(AssignmentCreateDto assignment) { // 학생에게 알림 보낼 dto 생성 후 넣기
        try {
            String json = objectMapper.writeValueAsString(assignment);

            //ObjectMapper(ofObject) vs MapRecord(ofMap)
            //
            MapRecord<String, String, String> record =
                    StreamRecords.newRecord()
                            .ofMap(Map.of("payload", json))//payload 설정
                            .withStreamKey(ASSIGNMENT_PUB.getKey());//스트림 키 지정
            log.info("publish event");
            redisTemplate.opsForStream().add(record);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
