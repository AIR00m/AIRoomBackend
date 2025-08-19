package com.airoom.airoom.common.aop.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AgentGuardInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();
        // install/agent-required/login 등은 예외 처리
        if (uri.startsWith("/install") || uri.startsWith("/agent-required") || uri.startsWith("/login") || uri.startsWith("/api/agent")) {
            return true;
        }
        HttpSession session = req.getSession(false);
        Boolean verified = (session == null) ? null : (Boolean) session.getAttribute("AGENT_VERIFIED");
        if (verified == null || !verified) {
            res.sendRedirect("/agent-required");
            return false;
        }
        return true;
    }
}