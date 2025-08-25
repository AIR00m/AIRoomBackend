package com.airoom.airoom.common.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // 같은 요청에 한번만 실행하게 되는 필터

    private final JWTTokenUtility jwtTokenUtility; // 토큰 생성 + 검증하는 것
    private final PathMatcher pathMatcher; // 주로 특정 경로 패턴이 주어진 경로와 일치하는지 확인할때 사용
    private final List<String> whiteList = List.of(
            "/", "/index.html",
            "/api/agent/**",
            "/download/agent/**",
            "/install/**",
            "/agent-required/**",
            "/auth/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"); // 필터에 적용 받지 않을 위치 경로를 추가


    public JwtAuthenticationFilter(JWTTokenUtility jwtTokenUtility, PathMatcher pathMatcher) {
        this.jwtTokenUtility = jwtTokenUtility;
        this.pathMatcher = (pathMatcher != null) ? pathMatcher : new AntPathMatcher();
    }

    // 화이트 리스트에 된 것에는 필터를 씌우지 않는다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        // contextPath airoom 같은 것이 존재하면 앞에 airoom을 붙여서 사용을 한다.
        for (String list : whiteList) {
            if (pathMatcher.match(list, uri)) {
                return true;
            }
        }
        return false;
    }

    // 로그인이후에 모든 요청에 이 필터가 사용이 되는거고
    // 요청만다 토큰에 들어가서 권한과 회원자 id를 확인
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            // 1. 해더에서 Authorization을 가져온다.(이곳에 Access Token이 존재)
            String token = request.getHeader("Authorization");

            // 2. 토큰이 없거나 Bearer로 시작하지 않으면 다음 필터로 넘겨 준다.
            if (token == null || !token.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }
            // 3. Access 토큰을 검증하는 메소드를 진행한다.
            Claims claim = jwtTokenUtility.verifyAccessToken(token);

            // 4. 권한을 만들어주기
            String username = claim.getSubject(); // 회원아이디
            String role = claim.get("role", String.class); // 회원 역할(선생님,학생)
            Long classroomNo = claim.get("classroomNo", Long.class);
            String authority = "ROLE_" + role.toUpperCase();

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
            //   사용자가 가진 권한 예)ROLE_TEACHER 같은것 -> List는 권한이 여러개 가능하므로

            CustomUserDetails userDetails = new CustomUserDetails(username,classroomNo,authorities);

            // 5. 권한을 기반으로 출입증 만들기
            UsernamePasswordAuthenticationToken authentication
                    = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            // 토큰 기반이라서 비밀번호가 필요 없음

            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Security Context에 인증상태를 기록 한다.

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response);
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401인증 실패
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }


}


