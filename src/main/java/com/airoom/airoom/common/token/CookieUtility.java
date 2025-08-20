package com.airoom.airoom.common.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtility {

    private final boolean secure;

    public CookieUtility(@Value("${app.cookie.secure:false}")boolean secure) {
        this.secure = secure;
    }

    // HttpOnly RefreshToken 만들기
    // ResponseCookie 스프링이 만든 응답 전용 쿠키 객체 HttpHeaders에 Set-Cookie 헤더를 넣을 때 사용
    public ResponseCookie refreshTokenCookie(String refreshToken) {
        String sameSite = secure ? "None" : "Lax"; // HTTP 에선 None 금지 → 자동 보정
        return ResponseCookie.from("Refresh_Token", refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    // 쿠키를 삭제하기
    // SET_COOKIE 헤더를 보내면 브라우저가 그 쿠키를 즉시 삭제한다.
    // Name, Path, Domain 로 보내야 정확히 지워진다.
    public ResponseCookie deleteTokenCookie() {
        String sameSite = secure ? "None" : "Lax";
        return ResponseCookie.from("Refresh_Token", "") // <- 이름
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/auth/refresh") // <- 경로
                .maxAge(0)
                .build();
    }


}
