package com.airoom.airoom.presence;

import com.airoom.airoom.presence.dto.FocusCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
     * 1. 새로운 WebSocket 연결 감지 (학생 입장 처리)
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
     * 2. 학생 클라이언트의 주기적인 하트비트 처리
     */
    @MessageMapping("/presence.heartbeat")
    public void heartbeat(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if(attrs == null) return;

        Long classNo = (Long) attrs.get("classNo");
        String userId = (String) attrs.get("userId");

        if (classNo == null || userId == null) {
            log.warn("heartbeat() missing classNo or userId in session attributes");
            return;
        }
        presenceService.heartbeat(classNo, userId);
    }

    /**
     * 3. WebSocket 연결 종료 감지 (학생 퇴장 처리)
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

    /**
     * 4. 선생님의 '집중학습 모드' 시작 요청 처리
     */
    @MessageMapping("/presence.focus.start")
    public void startFocus(@Payload FocusCommand command, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs == null) return;

        String role = (String) attrs.get("role");
        Long classNo = (Long) attrs.get("classNo");

        if ("teacher".equals(role) && classNo != null && command.unitNo() != null) {
            presenceService.startFocusMode(classNo, command.unitNo());
        } else {
            log.warn("Unauthorized or invalid focus start request. Role: {}, ClassNo: {}", role, classNo);
        }
    }

    /**
     * 5. 선생님의 '집중학습 모드' 종료 요청 처리
     */
    @MessageMapping("/presence.focus.stop")
    public void stopFocus(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs == null) return;

        String role = (String) attrs.get("role");
        Long classNo = (Long) attrs.get("classNo");

        if ("teacher".equals(role) && classNo != null) {
            presenceService.stopFocusMode(classNo);
        } else {
            log.warn("Unauthorized or invalid focus stop request. Role: {}, ClassNo: {}", role, classNo);
        }
    }

    /**
     * 6. (신규) 선생님 클라이언트로부터 주기적인 펄스 수신
     */
    @MessageMapping("/presence.focus.pulse")
    public void pulseFocus(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs == null) return;

        String role = (String) attrs.get("role");
        Long classNo = (Long) attrs.get("classNo");

        if ("teacher".equals(role) && classNo != null) {
            presenceService.triggerFocusPulse(classNo);
        }
    }
}