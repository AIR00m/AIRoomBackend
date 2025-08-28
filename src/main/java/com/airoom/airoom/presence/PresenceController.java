package com.airoom.airoom.presence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    /**
     * 1. 새로운 WebSocket 연결을 감지하여 입장 처리를 합니다.
     * - Interceptor가 세션에 저장해둔 role, classNo, userId를 꺼내 사용합니다.
     * - 역할이 'student'일 경우, presenceService.enter()를 호출하여 온라인 상태를 Redis에 저장하고
     * 다른 클라이언트들에게 'online: true' 이벤트를 브로드캐스트합니다.
     */
    @EventListener
    public void onConnect(SessionConnectedEvent e) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(e.getMessage());
        Map<String, Object> attrs = acc.getSessionAttributes();
        if (attrs == null) return;

        String role = (String) attrs.get("role");
        Long classNo = (Long) attrs.get("classNo");
        String userId = (String) attrs.get("userId");

        if ("student".equals(role) && classNo != null && userId != null) {
            log.info("### Student Connected via EventListener: userId={}, classNo={}", userId, classNo);
            presenceService.enter(classNo, userId);
        }
    }

    /**
     * 2. 학생 클라이언트가 주기적으로 보내는 하트비트 메시지를 처리합니다.
     * - 학생이 온라인 상태를 유지하고 있음을 Redis에 계속 갱신합니다.
     */
    @MessageMapping("/presence.heartbeat")
    public void heartbeat(SimpMessageHeaderAccessor accessor,
                          @Header(name = "classNo", required = false) String classNoHeader,
                          @Header(name = "classId", required = false) String legacyClassIdHeader,
                          @Header(name = "userId",  required = false) String headerUserId) {

        Long classNo = coalesceClassNo(classNoHeader, legacyClassIdHeader,
                (Long) accessor.getSessionAttributes().get("classNo"));
        String userId = headerUserId != null ? headerUserId :
                (String) accessor.getSessionAttributes().get("userId");

        if (classNo == null || userId == null) {
            log.warn("heartbeat() missing classNo or userId");
            return;
        }
        presenceService.heartbeat(classNo, userId);
    }

    /**
     * 3. WebSocket 연결 종료를 감지하여 오프라인 처리를 합니다.
     * - 역할이 'student'일 경우, presenceService.offlineNow()를 호출하여
     * 다른 클라이언트들에게 'online: false' 이벤트를 브로드캐스트합니다.
     */
    @EventListener
    public void onDisconnect(SessionDisconnectEvent e) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(e.getMessage());
        Map<String, Object> attrs = acc.getSessionAttributes();
        if (attrs == null) return;

        String role = (String) attrs.get("role");
        Long classNo = (Long) attrs.get("classNo");
        String userId = (String) attrs.get("userId");

        if ("student".equals(role) && classNo != null && userId != null) {
            log.info("### Student Disconnected: userId={}, classNo={}", userId, classNo);
            presenceService.offlineNow(classNo, userId);
        }
    }

    private Long coalesceClassNo(String classNoHeader, String legacyClassIdHeader, Long sessionVal) {
        Long h1 = parseLong(classNoHeader);
        if (h1 != null) return h1;
        Long h2 = parseLong(legacyClassIdHeader);
        if (h2 != null) return h2;
        return sessionVal;
    }

    private Long parseLong(String s) {
        if (s == null) return null;
        try { return Long.valueOf(s); } catch (NumberFormatException e) { return null; }
    }
}