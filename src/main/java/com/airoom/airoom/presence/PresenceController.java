package com.airoom.airoom.presence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @MessageMapping("/presence.enter")
    public void enter(SimpMessageHeaderAccessor accessor,
                      @Header(name = "classNo", required = false) String classNoHeader,
                      @Header(name = "classId", required = false) String legacyClassIdHeader,
                      @Header(name = "userId",  required = false) String headerUserId) {

        Long classNo = coalesceClassNo(classNoHeader, legacyClassIdHeader,
                (Long) accessor.getSessionAttributes().get("classNo"));
        String userId = headerUserId != null ? headerUserId :
                (String) accessor.getSessionAttributes().get("userId");

        if (classNo == null || userId == null) {
            log.warn("enter() missing classNo or userId");
            return;
        }
        presenceService.enter(classNo, userId);
    }

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

    @EventListener
    public void onDisconnect(SessionDisconnectEvent e) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(e.getMessage());
        Map<String, Object> attrs = acc.getSessionAttributes();
        if (attrs == null) return;

        String role = (String) attrs.get("role");
        Long classNo = (Long) attrs.get("classNo");
        String userId = (String) attrs.get("userId");

        if ("student".equals(role) && classNo != null && userId != null) {
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
