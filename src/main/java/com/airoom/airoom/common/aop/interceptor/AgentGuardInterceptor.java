package com.airoom.airoom.common.aop.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AgentGuardInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();

        // 예외 경로: 설치/안내/로그인/에이전트 API/다운로드/정적
        if (uri.startsWith("/install")
                || uri.startsWith("/agent-required")
                || uri.startsWith("/login")
                || uri.startsWith("/api/agent")
                || uri.startsWith("/download")
                || uri.startsWith("/assets")        // Vite 정적
                || uri.startsWith("/favicon")       // 파비콘
                || uri.startsWith("/error")      // 에러 페이지
                || uri.startsWith("/css")       // 추가
                || uri.startsWith("/js")        // 추가
                || uri.startsWith("/images")    // 추가
                || uri.startsWith("/webjars")){   // (스웨거/웹자르)
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