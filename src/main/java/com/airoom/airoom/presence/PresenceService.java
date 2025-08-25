package com.airoom.airoom.presence;

import com.airoom.airoom.presence.dto.PresenceEvent;
import com.airoom.airoom.presence.dto.PresenceListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messaging;

    // 온라인 판정(하트비트 주기보다 살짝 크게), Redis TTL(여유)
    private static final long ONLINE_WINDOW_MS = 35_000;
    private static final long TTL_SECONDS = 120;

    private String key(long classNo, String userId) {
        return "presence:%d:%s".formatted(classNo, userId);
    }

    private boolean isOnline(long lastSeen) {
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
        // 정책 1: "방금 오프라인 됨" 의미로 now 사용(기본)
        broadcast(classNo, userId, false, now);

        // 정책 2(선택): 직전 하트비트 시각으로 보내고 싶으면 아래 사용
        // String v = redis.opsForValue().get(key(classNo, userId));
        // long last = (v != null) ? Long.parseLong(v) : now;
        // broadcast(classNo, userId, false, last);
    }

    /** 반 스냅샷: Redis 값(lastSeen) 읽어 온라인 여부 계산 */
    public List<PresenceListItem> snapshot(long classNo) {
        String pattern = "presence:" + classNo + ":*";
        List<String> keys = new ArrayList<>();

        redis.execute((RedisConnection conn) -> {
            try (var cursor = conn.keyCommands().scan(
                    ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                cursor.forEachRemaining(b -> keys.add(new String(b, StandardCharsets.UTF_8)));
            }
            return null;
        });

        if (keys.isEmpty()) return Collections.emptyList();

        List<String> values = redis.opsForValue().multiGet(keys);
        if (values == null) values = Collections.emptyList();

        List<PresenceListItem> out = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            String[] parts = keys.get(i).split(":"); // presence:{classNo}:{userId}
            if (parts.length != 3) continue;

            long cNo;
            try { cNo = Long.parseLong(parts[1]); } catch (Exception e) { continue; }
            String uId = parts[2];

            long last = 0L;
            try {
                String v = (i < values.size()) ? values.get(i) : null;
                last = (v != null) ? Long.parseLong(v) : 0L;
            } catch (Exception ignore) {}

            boolean online = last > 0 && isOnline(last);
            out.add(new PresenceListItem(uId, cNo, online, last));
        }

        out.sort(Comparator.<PresenceListItem, Boolean>comparing(PresenceListItem::online).reversed()
                .thenComparing(PresenceListItem::userId));
        return out;
    }

    /** 교사에게 브로드캐스트: /topic/presence.{classNo} */
    private void broadcast(long classNo, String userId, boolean online, long lastSeen) {
        PresenceEvent evt = new PresenceEvent(userId, classNo, online, lastSeen);
        messaging.convertAndSend("/topic/presence." + classNo, evt);
    }
}
