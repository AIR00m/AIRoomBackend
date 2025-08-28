package com.airoom.airoom.presence;

import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.presence.dto.PresenceEvent;
import com.airoom.airoom.presence.dto.PresenceListItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messaging;
    // =========================== [변경점 1: Repository 주입] ===========================
    private final ClassroomStudentRepository classroomStudentRepository;
    // =================================================================================

    // 온라인 판정(하트비트 주기보다 살짝 크게), Redis TTL(여유)
    private static final long ONLINE_WINDOW_MS = 10_000;
    private static final long TTL_SECONDS = 60;

    private String key(long classNo, String userId) {
        return "presence:%d:%s".formatted(classNo, userId);
    }

    private boolean isOnline(long lastSeen) {
        // [수정] lastSeen이 0일 경우를 대비한 방어 코드 추가
        if (lastSeen == 0L) return false;
        return (Instant.now().toEpochMilli() - lastSeen) < ONLINE_WINDOW_MS;
    }

    /** 입장: 저장 + 온라인 브로드캐스트 */
    public void enter(long classNo, String userId) {
        long now = Instant.now().toEpochMilli();
        redis.opsForValue().set(key(classNo, userId), Long.toString(now), TTL_SECONDS, TimeUnit.SECONDS);
        broadcast(classNo, userId, true, now);
    }

    /** 하트비트: 오프→온 전이에서만 온라인 브로드캐스트 */
    public void heartbeat(long classNo, String userId) {
        String k = key(classNo, userId);
        Long prev = null;
        try {
            String v = redis.opsForValue().get(k);
            if (v != null) prev = Long.parseLong(v);
        } catch (Exception ignore) {}
        boolean wasOnline = prev != null && isOnline(prev);

        long now = Instant.now().toEpochMilli();
        redis.opsForValue().set(k, Long.toString(now), TTL_SECONDS, TimeUnit.SECONDS);

        if (!wasOnline) {
            broadcast(classNo, userId, true, now);
        }
    }

    /** (선택) 즉시 오프라인 알림 */
    public void offlineNow(long classNo, String userId) {
        long now = Instant.now().toEpochMilli();
        broadcast(classNo, userId, false, now);
    }

    // =========================== [변경점 2: snapshot 메서드 교체] ===========================
    /** 반 스냅샷: DB에서 전체 학생 목록을 가져온 뒤, Redis 값으로 온라인 여부 계산 */
    public List<PresenceListItem> snapshot(long classNo) {
        // 1. DB에서 해당 반의 '전체 학생 목록'을 DTO 형태로 가져옵니다.
        List<ClassroomStudentResponse> allStudentsInClass = classroomStudentRepository.findClassroomStudentsByClassroomNo(classNo);

        // 2. 각 학생 DTO에 대해 Redis에서 온라인 상태를 확인하여 최종 목록을 만듭니다.
        return allStudentsInClass.stream()
                .map(studentDto -> {
                    String userId = studentDto.studentId(); // DTO에서 학생 ID 가져오기
                    String redisKey = key(classNo, userId);

                    String lastSeenStr = redis.opsForValue().get(redisKey);

                    long lastSeen = 0L;
                    if (lastSeenStr != null) {
                        try {
                            lastSeen = Long.parseLong(lastSeenStr);
                        } catch (NumberFormatException e) {
                            // 값 변환 실패 시 0으로 처리 (오프라인)
                        }
                    }

                    boolean isOnline = isOnline(lastSeen);

                    // PresenceListItem DTO 형식에 맞게 반환
                    return new PresenceListItem(userId, classNo, isOnline, lastSeen, studentDto.studentName());
                })
                .collect(Collectors.toList());
    }
    // =================================================================================

    /** 교사에게 브로드캐스트: /topic/presence.{classNo} */
    private void broadcast(long classNo, String userId, boolean online, long lastSeen) {
        log.info("Broadcasting event -> to: /topic/presence.{}, userId: {}, online: {}", classNo, userId, online);
        PresenceEvent evt = new PresenceEvent(userId, classNo, online, lastSeen);
        messaging.convertAndSend("/topic/presence." + classNo, evt);
    }
}