package com.airoom.airoom.presence;

import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.presence.dto.PresenceEvent;
import com.airoom.airoom.presence.dto.PresenceListItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messaging;
    private final ClassroomStudentRepository classroomStudentRepository;

    /**
     * 현재 활성화된 집중학습 모드를 저장하는 맵
     * Key: classNo (반 번호)
     * Value: unitNo (이동시킬 단원 번호)
     */
    private final Map<Long, Long> activeFocusModes = new ConcurrentHashMap<>();

    private static final long ONLINE_WINDOW_MS = 10_000;
    private static final long TTL_SECONDS = 60;

    private String key(long classNo, String userId) {
        return "presence:%d:%s".formatted(classNo, userId);
    }

    private boolean isOnline(long lastSeen) {
        if (lastSeen == 0L) return false;
        return (Instant.now().toEpochMilli() - lastSeen) < ONLINE_WINDOW_MS;
    }

    private boolean isUserOnline(long classNo, String userId) {
        String lastSeenStr = redis.opsForValue().get(key(classNo, userId));
        if (lastSeenStr == null) return false;
        try {
            long lastSeen = Long.parseLong(lastSeenStr);
            return isOnline(lastSeen);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void enter(long classNo, String userId) {
        long now = Instant.now().toEpochMilli();
        redis.opsForValue().set(key(classNo, userId), Long.toString(now), TTL_SECONDS, TimeUnit.SECONDS);
        broadcast(classNo, userId, true, now);
    }

    public void heartbeat(long classNo, String userId) {
        String k = key(classNo, userId);
        String v = redis.opsForValue().get(k);
        boolean wasOnline = false;
        if (v != null) {
            try {
                wasOnline = isOnline(Long.parseLong(v));
            } catch (NumberFormatException ignored) {}
        }

        long now = Instant.now().toEpochMilli();
        redis.opsForValue().set(k, Long.toString(now), TTL_SECONDS, TimeUnit.SECONDS);

        if (!wasOnline) {
            broadcast(classNo, userId, true, now);
        }
    }

    public void offlineNow(long classNo, String userId) {
        long now = Instant.now().toEpochMilli();
        broadcast(classNo, userId, false, now);
    }

    @Transactional(readOnly = true)
    public List<PresenceListItem> snapshot(long classNo) {
        List<ClassroomStudentResponse> allStudentsInClass = classroomStudentRepository.findClassroomStudentsByClassroomNo(classNo);
        return allStudentsInClass.stream()
                .map(studentDto -> {
                    String userId = studentDto.studentId();
                    String lastSeenStr = redis.opsForValue().get(key(classNo, userId));
                    long lastSeen = 0L;
                    if (lastSeenStr != null) {
                        try {
                            lastSeen = Long.parseLong(lastSeenStr);
                        } catch (NumberFormatException ignored) {}
                    }
                    return new PresenceListItem(userId, classNo, isOnline(lastSeen), lastSeen, studentDto.studentName());
                })
                .collect(Collectors.toList());
    }

    private void broadcast(long classNo, String userId, boolean online, long lastSeen) {
        log.info("Broadcasting presence event -> to: /topic/presence.{}, userId: {}, online: {}", classNo, userId, online);
        PresenceEvent evt = new PresenceEvent(userId, classNo, online, lastSeen);
        messaging.convertAndSend("/topic/presence." + classNo, evt);
    }

    public void startFocusMode(long classNo, long unitNo) {
        log.info("### Storing FOCUS mode state: classNo={}, unitNo={}", classNo, unitNo);
        activeFocusModes.put(classNo, unitNo);
    }

    public void stopFocusMode(long classNo) {
        log.info("### Clearing FOCUS mode state and broadcasting STOP: classNo={}", classNo);
        activeFocusModes.remove(classNo);
        Map<String, Object> payload = Map.of("eventType", "FOCUS_STOP");
        messaging.convertAndSend("/topic/presence." + classNo, payload);
    }

    public void triggerFocusPulse(long classNo) {
        Long unitNo = activeFocusModes.get(classNo);
        if (unitNo == null) {
            log.warn("Received a pulse for a non-active focus mode class: {}", classNo);
            return;
        }

        List<ClassroomStudentResponse> allStudents = classroomStudentRepository.findClassroomStudentsByClassroomNo(classNo);
        List<String> onlineUserIds = allStudents.stream()
                .map(ClassroomStudentResponse::studentId)
                .filter(userId -> isUserOnline(classNo, userId))
                .toList();

        if (!onlineUserIds.isEmpty()) {
            log.info("Triggering FOCUS_PULSE for class {} to {} students", classNo, onlineUserIds.size());
            Map<String, Object> payload = Map.of(
                    "eventType", "FOCUS_PULSE",
                    "unitNo", unitNo
            );
            messaging.convertAndSend("/topic/presence." + classNo, payload);
        }
    }
}