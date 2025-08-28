package com.airoom.airoom.common.aop.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

public class AgentGuardInterceptor implements HandlerInterceptor {

    private static final String PROD_REDIRECT = "http://43.200.2.244:80/agent-required";
    private static final String DEV_REDIRECT  = "http://localhost:5173/agent-required";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        final String uri = req.getRequestURI();

        // [NEW] 로컬 백엔드에서의 모든 요청은 전면 우회
        if (isLocalRequest(req)) return true;

        // 1) 기본 예외
        if (isExcluded(uri)) return true;

        // 2) Swagger Try it out 예외
        if (isSwaggerReferrer(req)) return true;

        // 3) 일반 가드
        HttpSession session = req.getSession(false);
        boolean verified = (session != null) && Boolean.TRUE.equals(session.getAttribute("AGENT_VERIFIED"));
        if (!verified) {
            // 프런트가 406 & {location:"/agent-required"}를 보고 라우팅하도록 설계됨
            res.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            res.setContentType("application/json;charset=UTF-8");
            Map<String,String> body=Map.of("location","/agent-required");
            res.getWriter().print(new ObjectMapper().writeValueAsString(body));
//            String redirect = isLocalRequest(req) ? DEV_REDIRECT : PROD_REDIRECT;
//            res.sendRedirect(redirect);
            return false;
        }
        return true;
    }

    private boolean isSwaggerReferrer(HttpServletRequest req) {
        String ref = req.getHeader("Referer");
        return ref != null && ref.contains("/swagger-ui");
    }

    /** 스웨거/정적/설치 등 예외 경로 */
    private boolean isExcluded(String uri) {
        return startsAny(uri,
                // 기존 예외
                "/install",
                "/agent-required",
                "/auth",
                "/api/agent",
                "/download",
                "/forensic",
                "/api/forensic",
                "/assets",
                "/favicon",
                "/error",
                "/css",
                "/js",
                "/images",
                "/webjars",
                // Swagger / OpenAPI (springdoc)
                "/swagger-ui.html",
                "/swagger-ui",          // /swagger-ui, /swagger-ui/index.html, /swagger-ui/** 포함
                "/swagger-resources",   // 일부 환경 호환
                "/v3/api-docs",
                // 호환/커스텀 경로 대비 (있을 수 있으니 안전망)
                "/api-docs",
                "/v2/api-docs"
        );
    }

    private boolean startsAny(String uri, String... prefixes) {
        for (String p : prefixes) {
            if (uri.startsWith(p)) return true;
        }
        return false;
    }

    /** 요청의 Host 정보를 기준으로 로컬 개발 환경 여부 판별 */
    private boolean isLocalRequest(HttpServletRequest req) {
        String host = firstNonEmpty(
                req.getHeader("X-Forwarded-Host"),
                req.getHeader("Host"),
                req.getServerName()
        );
        if (host == null) return false;

        host = host.toLowerCase();

        if (host.contains("localhost") || host.contains("127.0.0.1") || host.startsWith("0.0.0.0")) return true;
        if (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172.16.") || host.startsWith("172.17.")
                || host.startsWith("172.18.") || host.startsWith("172.19.") || host.startsWith("172.2")
                || host.endsWith(".local")) return true;

        if (host.contains("43.200.2.244")) return false;

        return false;
    }

    private String firstNonEmpty(String a, String b, String c) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        if (c != null && !c.isBlank()) return c;
        return null;
    }
}
