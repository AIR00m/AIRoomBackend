package com.airoom.airoom.presence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PresenceHeaderInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
        if (acc.getCommand() == StompCommand.CONNECT) {
            Map<String, Object> sessionAttributes = acc.getSessionAttributes();
            if(sessionAttributes == null) return message;

            // 문자열 헤더 저장
            putIfPresent(acc, "role");
            putIfPresent(acc, "userId");

            // classNo(Long) 저장 (legacy: classId 도 허용)
            Long classNo = parseLong(firstHeader(acc, "classNo", "classId"));
            if (classNo != null) {
                sessionAttributes.put("classNo", classNo);
            }

            log.debug("STOMP CONNECT - role={}, classNo={}, userId={}",
                    sessionAttributes.get("role"),
                    sessionAttributes.get("classNo"),
                    sessionAttributes.get("userId"));
        }
        return message;
    }

    private void putIfPresent(StompHeaderAccessor acc, String name) {
        Map<String, Object> sessionAttributes = acc.getSessionAttributes();
        if(sessionAttributes == null) return;

        String val = firstHeader(acc, name);
        if (val != null) sessionAttributes.put(name, val);
    }

    @Nullable
    private String firstHeader(StompHeaderAccessor acc, String... names) {
        for (String n : names) {
            List<String> vals = acc.getNativeHeader(n);
            if (vals != null && !vals.isEmpty()) return vals.get(0);
        }
        return null;
    }

    @Nullable
    private Long parseLong(@Nullable String s) {
        if (s == null) return null;
        try { return Long.valueOf(s); } catch (NumberFormatException e) { return null; }
    }
}