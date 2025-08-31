package com.airoom.airoom.notification.model.service;

import com.airoom.airoom.member.model.repository.MemberRepository;
import com.airoom.airoom.notification.model.dto.NotificationDto;
import com.airoom.airoom.notification.model.repository.EmitterRepository;
import com.airoom.airoom.notification.model.repository.StoredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmitterService {

    private final EmitterRepository emitterRepository;
    private final AtomicLong idGen = new AtomicLong(System.currentTimeMillis());

    public SseEmitter connectEmitter(Long memberNo, @Nullable Long lastEventId) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        emitter.onCompletion(() -> {
            emitterRepository.removeEmitter(memberNo);
            log.info("SSE onCompletion - memberNo: {}", memberNo);
        });

        emitter.onTimeout(() -> {
            emitterRepository.removeEmitter(memberNo);
            log.info("SSE onTimeout - memberNo: {}", memberNo);
        });

        emitter.onError(e -> {
            emitterRepository.removeEmitter(memberNo);
            log.warn("SSE onError - memberNo: {}", memberNo, e);
            try { emitter.completeWithError(e); } catch (IllegalStateException ignore) {}
        });

        emitterRepository.saveEmitter(memberNo, emitter);

        // 1) 초기 연결 이벤트
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully")
                    .reconnectTime(3000));
        } catch (IOException e) {
            emitterRepository.removeEmitter(memberNo);
            try { emitter.completeWithError(e); } catch (IllegalStateException ignore) {}
            log.warn("SSE 초기 연결 실패 - memberNo: {}", memberNo, e);
            return emitter;
        }

        // 2) 끊긴 사이의 이벤트 재전송
        if (lastEventId != null && lastEventId >= 0) {
            List<StoredEvent> missed = emitterRepository.getEventsAfter(memberNo, lastEventId);
            if (!missed.isEmpty()) {
                log.info("미전송 {}건 재전송 - memberNo: {}, lastEventId: {}", missed.size(), memberNo, lastEventId);
                for (StoredEvent ev : missed) {
                    try {
                        emitter.send(SseEmitter.event()
                                .id(String.valueOf(ev.id()))
                                .name(ev.name())
                                .data(ev.data(), MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        log.warn("재전송 실패 - memberNo: {}, eventId: {}", memberNo, ev.id(), e);
                        break; // 연결 상태 불안정 -> 콜백에서 정리됨
                    }
                }
            }
        }

        return emitter;
    }

    public void sendNotificationToMember(Long memberNo, NotificationDto notification) {
        if (notification == null) {
            log.warn("null Notification 전달 시도 - memberNo: {}", memberNo);
            return;
        }

        // 1) 이벤트 ID 부여 & 버퍼 선기록(연결 없어도 유실 방지)
        long eid = idGen.incrementAndGet();
        emitterRepository.appendEvent(memberNo, new StoredEvent(eid, "notification", notification));

        // 2) 현재 연결에 즉시 전송
        SseEmitter emitter = emitterRepository.getEmitter(memberNo);
        if (emitter == null) {
            log.debug("SSE 연결 없음(버퍼에 저장됨) - memberNo: {}", memberNo);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(eid))
                    .name("notification")
                    .data(notification, MediaType.APPLICATION_JSON));
            log.info("SSE 알림 전송 성공 - memberNo: {}, eventId: {}", memberNo, eid);
        } catch (IOException e) {
            log.warn("SSE 알림 전송 실패 - memberNo: {}, eventId: {}", memberNo, eid, e);
            emitterRepository.removeEmitter(memberNo);
            try { emitter.completeWithError(e); } catch (IllegalStateException ignore) {}
        }
    }

    // -------- 하트비트(주석 이벤트) --------
    // @EnableScheduling 필요
    @Scheduled(fixedDelay = 25000)
    public void heartbeat() {
        for (Map.Entry<Long, SseEmitter> entry : emitterRepository.allEmitters()) {
            Long memberNo = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                // 주석(: hb) 전송 -> 프론트 리스너 발동 안 함
                emitter.send(SseEmitter.event().comment("hb"));
            } catch (IOException e) {
                log.warn("하트비트 실패 - memberNo: {}", memberNo, e);
                emitterRepository.removeEmitter(memberNo);
                try { emitter.completeWithError(e); } catch (IllegalStateException ignore) {}
            }
        }
    }
}
