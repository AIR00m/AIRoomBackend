package com.airoom.airoom.notification.model.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Component
public class EmitterRepository {

    private static final int MAX_BUFFER = 500;

    private final Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    // 사용자별 최근 이벤트 버퍼
    private final Map<Long, Deque<StoredEvent>> eventBuffers = new ConcurrentHashMap<>();

    public void saveEmitter(Long memberNo, SseEmitter emitter) {
        SseEmitter prev = sseEmitterMap.put(memberNo, emitter);
        if (prev != null) {
            log.info("SSE 기존 연결 교체 - memberNo: {}", memberNo);
        } else {
            log.info("SSE 연결 저장 - memberNo: {}", memberNo);
        }
    }

    public void removeEmitter(Long memberNo) {
        SseEmitter removed = sseEmitterMap.remove(memberNo);
        if (removed != null) {
            log.info("SSE 연결 제거(참조만) - memberNo: {}", memberNo);
        }
    }

    public SseEmitter getEmitter(Long memberNo) {
        return sseEmitterMap.get(memberNo);
    }

    public Collection<Map.Entry<Long, SseEmitter>> allEmitters() {
        return Collections.unmodifiableSet(sseEmitterMap.entrySet());
    }

    // ----- 이벤트 버퍼 -----
    public void appendEvent(Long memberNo, StoredEvent ev) {
        Deque<StoredEvent> q = eventBuffers.computeIfAbsent(memberNo, k -> new ConcurrentLinkedDeque<>());
        q.addLast(ev);
        while (q.size() > MAX_BUFFER) q.pollFirst();
    }

    public List<StoredEvent> getEventsAfter(Long memberNo, long lastEventId) {
        Deque<StoredEvent> q = eventBuffers.get(memberNo);
        if (q == null || q.isEmpty()) return Collections.emptyList();
        List<StoredEvent> out = new ArrayList<>();
        for (StoredEvent e : q) {
            if (e.id() > lastEventId) out.add(e);
        }
        return out;
    }
}
