package com.bs.boot.myboardrest.project.config.token;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class JWTTokenUtility {

    // 환경 변수에 있는 Secret Key 가지고 오기 -> 여기서만 사용하므로 접근 제한자는 private를 사용해서 할것
    private static final String SECRET_KEY =  Optional.ofNullable(System.getenv("JWT_SECRET_KEY"))
            .orElseThrow(() -> new IllegalStateException("환경변수 JWT_SECRET_KEY가 설정되어 있지 않습니다."));
    private static final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    // 컴퓨터는 문자를 이해못함 -> 바이트 배열로 형태로 변환(UTF_8방식으로 변환)
    // 이런 순수 바이트 배열 사용 X -> JJWT 는 HMAC-SHA 알고리즘을 사용해서 알고리즘에 맞는 객체로 반환해준다.
    private static final String AIROOM_ADDRESS = "http://43.200.2.244:8080/airoom";
    private static final Long ACCESS_TOKEN_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(30);
    private static final Long REFRESH_TOKEN_EXPIRATION_TIME = TimeUnit.DAYS.toMillis(7);

    public String createAccessToken(String userId, boolean isTeacher) {
        HashMap<String, Object> claims = new HashMap<>();
        String role = isTeacher ? "teacher" : "student";
        claims.put("role", role);
        claims.put("token_type", "AccessToken");
        return buildToken(userId, claims, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    public String createRefreshToken(String userId) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("token_type", "RefreshToken");
        return buildToken(userId, claims, REFRESH_TOKEN_EXPIRATION_TIME);
    }

    private String buildToken(String userId, Map<String, Object> claims, Long expiration) {
        long currentTime = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims != null ? claims : new HashMap<>())
                .subject(userId) // 받는 주체
                .issuer(AIROOM_ADDRESS) // 발급자
                .issuedAt(new Date(currentTime)) // 토큰 생성일
                .expiration(new Date(currentTime + expiration)) // 토큰 만료 시간(초)
                .signWith(secretKey, Jwts.SIG.HS256) //  서명 + 알고리즘을 고정하면 JWT 헤더 변조 공격 방지
                .compact();
    }


    public static Claims verifyToken(String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            // contains로 하게 되면 중간에 포함된것도 true
            throw new JwtException("토큰 형식이 올바르지 않습니다.");
        }
        // Replace로 하게 되면 Bearer -> 여러개 있을경우 전부 교체 되는 상황이 발생
        token = token.substring(7);

        Claims claims = Jwts.parser() // 11 버전까지는 parserBuilder를 통해서 파싱함 -> 12버전은 parser로
                .verifyWith(secretKey) // -> 검증 키 등록
                .build()
                .parseSignedClaims(token) // -> 토큰을 파싱하고 -> 서명 검증까지 해줌
                .getPayload();

        String userId = claims.getSubject();

        if (userId == null || userId.isEmpty()) {
            throw new JwtException("아이디가 토큰에 존재하지 않습니다.");
        }
        if (!AIROOM_ADDRESS.equals(claims.getIssuer())) {
            throw new JwtException("발급자가 올바르지 않습니다.");
        }
        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            throw new JwtException("토큰이 만료되었습니다.");
        }

        String tokenType = claims.get("token_type", String.class);
        if (!"AccessToken".equals(tokenType) && !"RefreshToken".equals(tokenType)) {
            throw new JwtException("알 수 없는 토큰 타입입니다.");
        }

        return claims;
    }

}
