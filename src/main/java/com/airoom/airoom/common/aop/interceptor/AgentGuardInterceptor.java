package com.airoom.airoom.common.aop.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AgentGuardInterceptor implements HandlerInterceptor {

    private static final String PROD_REDIRECT = "http://43.200.2.244:80/agent-required";
    private static final String DEV_REDIRECT  = "http://127.0.0.1:5173/agent-required";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();

        // 예외 경로: 설치/안내/로그인/에이전트 API/다운로드/정적
        if (uri.startsWith("/install")
                || uri.startsWith("/agent-required")
                || uri.startsWith("/login")
                || uri.startsWith("/api/agent")
                || uri.startsWith("/download")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/assets")     // Vite 정적
                || uri.startsWith("/favicon")    // 파비콘
                || uri.startsWith("/error")      // 에러 페이지
                || uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/images")
                || uri.startsWith("/webjars")) {
            return true;
        }

        HttpSession session = req.getSession(false);
        Boolean verified = (session == null) ? null : (Boolean) session.getAttribute("AGENT_VERIFIED");

        if (verified == null || !verified) {
            String redirect = isLocalRequest(req) ? DEV_REDIRECT : PROD_REDIRECT;
            res.sendRedirect(redirect);
            return false; // 컨트롤러로 더 진행하지 않음
        }

        return true;
    }

    /** 요청의 Host 정보를 기준으로 로컬 개발 환경 여부 판별 */
    private boolean isLocalRequest(HttpServletRequest req) {
        // 프록시(Nginx) 뒤면 X-Forwarded-Host가 우선
        String host = firstNonEmpty(
                req.getHeader("X-Forwarded-Host"),
                req.getHeader("Host"),
                req.getServerName()
        );

        if (host == null) return false;

        host = host.toLowerCase();

        // 일반적인 로컬 패턴들
        if (host.contains("localhost") || host.contains("127.0.0.1") || host.startsWith("0.0.0.0")) return true;
        if (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172.16.") || host.startsWith("172.17.")
                || host.startsWith("172.18.") || host.startsWith("172.19.") || host.startsWith("172.2")
                || host.endsWith(".local")) return true;

        // 운영 IP/도메인이 명확하면 반대로 명시해도 됨
        if (host.contains("43.200.2.244")) return false;

        // 기본은 운영으로 판단
        return false;
    }

    private String firstNonEmpty(String a, String b, String c) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        if (c != null && !c.isBlank()) return c;
        return null;
    }
}
