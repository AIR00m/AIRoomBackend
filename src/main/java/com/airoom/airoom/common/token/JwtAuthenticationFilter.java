package com.airoom.airoom.common.token;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // 한번의 요청에 한번만 실행하게 되는 필터

    private final JWTTokenUtility jwtTokenUtility; // 토큰 생성 + 검증하는 것
    private final AntPathMatcher matcher = new AntPathMatcher(); // 경로 패턴을 매칭하는 도구
    private final List<String> whiteList =List.of(
            "/","index.html",
            "/auth/signup/*",
            "/auth/search/*",
            "/auth/login",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"); // 필터에 적용 받지 않을 위치 경로를 추가

    public JwtAuthenticationFilter(JWTTokenUtility jwtTokenUtility) {
        this.jwtTokenUtility = jwtTokenUtility;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 요청이 들어올 때, 실행되는 핵심구간1
        


    }

}
