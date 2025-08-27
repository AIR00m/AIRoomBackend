package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.entity.ChatRoom;
import com.airoom.airoom.chat.model.dto.ChatMessageRequest;
import com.airoom.airoom.chat.model.dto.ChatMessageResponse;
import com.airoom.airoom.chat.model.repository.ChatRoomRepository;
import com.airoom.airoom.common.value.MemberRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisBusyException;
import io.lettuce.core.RedisException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisSystemException;
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
    private final ChatMessageService messageService;
    private final ChatReadService readService;
    private final ChatRoomRepository roomRepository;
    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private Thread consumerThread;
    private volatile boolean running = true;

    private static final String STREAM = "chat:stream";
    private static final String GROUP = "chat-group";

    @PostConstruct
    public void initConsumer() {
        // 1. 그룹이 없으면 생성
        try {
            redis.opsForStream().createGroup(STREAM, ReadOffset.latest(), GROUP);
            System.out.println("Consumer Group created");
        } catch (RedisSystemException e) {
            if (e.getCause() instanceof RedisBusyException) {
                System.out.println("Consumer Group already exists");
            }
        }
        // 2. 이후 consume() 실행
        consume();
    }

    //@PostConstruct
    public void consume() {
        consumerThread = new Thread(() -> {
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

                            // 3. DB 저장
                            Long msgId = messageService.saveMessage(
                                    req.getCrNo(),
                                    req.getContent(),
                                    req.getWriterRole(),
                                    req.getSentAt()
                            );

                            MemberRole receiver = (req.getWriterRole() == MemberRole.TEACHER) ? MemberRole.STUDENT : MemberRole.TEACHER;
                            readService.incrementUnread(req.getCrNo(), receiver);

                            // 4. 브로드캐스트
                            ChatMessageResponse dto = ChatMessageResponse.builder()
                                    .crNo(req.getCrNo())
                                    .messageId(msgId)
                                    .content(req.getContent())
                                    .writerRole(req.getWriterRole())
                                    .sentAt(req.getSentAt())
                                    .build();
                            messagingTemplate.convertAndSend("/topic/chat/" + req.getCrNo(), dto);
                            sendUnreadNotification(req.getCrNo(), req.getWriterRole());
                            // 5. ACK (정상 처리 시)
                            redis.opsForStream().acknowledge(STREAM, GROUP, record.getId());

                        } catch (RedisSystemException e) {
                            if (e.getCause() instanceof RedisException && e.getCause().getMessage().contains("Connection closed")) {
                                System.out.println("Redis 연결 종료됨. 스레드 중지.");
                                break;
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        });
        consumerThread.start();
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        if (consumerThread != null && consumerThread.isAlive()) {
            consumerThread.interrupt();
        }
        System.out.println("ChatMessageConsumer thread 종료");
    }

    private void sendUnreadNotification(Long roomId, MemberRole senderRole) {
        try {
            // 채팅방 정보 조회
            ChatRoom room = roomRepository.findById(roomId).orElseThrow();

            // 상대방 결정 (보낸 사람이 아닌 상대방에게만 알림)
            if (senderRole == MemberRole.TEACHER) {
                // 선생님이 보낸 경우 → 학생에게 알림
                long unreadCount = readService.getTotalUnread(
                        room.getClassroomStudent().getClassRoomStudentNo(),
                        MemberRole.STUDENT
                );

                Map<String, Long> notification = Map.of("totalUnread", unreadCount);
                messagingTemplate.convertAndSend(
                        "/topic/unread/student/" + room.getClassroomStudent().getClassRoomStudentNo(),
                        notification
                );

            } else if (senderRole == MemberRole.STUDENT) {
                // 학생이 보낸 경우 → 선생님에게 알림
                long unreadCount = readService.getTotalUnread(
                        room.getClassroomTeacher().getClassroomTeacherNo(),
                        MemberRole.TEACHER
                );

                Map<String, Long> notification = Map.of("totalUnread", unreadCount);
                messagingTemplate.convertAndSend(
                        "/topic/unread/teacher/" + room.getClassroomTeacher().getClassroomTeacherNo(),
                        notification
                );
            }

        } catch (Exception e) {
            System.err.println("미읽음 알림 전송 실패: " + e.getMessage());
        }
    }
}
