package com.airoom.airoom.common.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@ConfigurationProperties(prefix = "jwt")
@Component
@RequiredArgsConstructor
public class JWTTokenUtility {

    // 환경 변수에 있는 Secret Key 가지고 오기 -> 여기서만 사용하므로 접근 제한자는 private를 사용해서 할것
    private static SecretKey secretKey; // final 아님 (초기화는 yml 값 이후)

    @Value("${jwt.secret}")
    private String secret;  // yml에서 읽어옴

    @PostConstruct
    public void init() {
        // yml에서 읽은 값을 static 필드에 변환 후 세팅
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    // 컴퓨터는 문자를 이해못함 -> 바이트 배열로 형태로 변환(UTF_8방식으로 변환)
    // 이런 순수 바이트 배열 사용 X -> JJWT 는 HMAC-SHA 알고리즘을 사용해서 알고리즘에 맞는 객체로 반환해준다.
    private static final String ISSUER = "http://43.200.2.244:8080/airoom";
    private static final Long ACCESS_TOKEN_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(30);
    private static final Long REFRESH_TOKEN_EXPIRATION_TIME = TimeUnit.DAYS.toMillis(7);

    public String createAccessToken(String userId, boolean isTeacher) {
        HashMap<String, Object> claims = new HashMap<>();
        String role = isTeacher ? "teacher" : "student";
        claims.put("role", role);
        claims.put("token_type", "AccessToken");
        return buildToken(userId, claims, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    public String createAccessToken(String userId, boolean isTeacher, Map<String, Object> extraClaims) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("token_type", "AccessToken");
        String role = isTeacher ? "teacher" : "student";
        claims.put("role", role);
        if (extraClaims != null && !extraClaims.isEmpty()) {
            claims.putAll(extraClaims);
        }
        return buildToken(userId, claims, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    public String createRefreshToken(String userId, boolean isTeacher) {
        HashMap<String, Object> claims = new HashMap<>();
        String role = isTeacher ? "teacher" : "student";
        claims.put("role", role);
        claims.put("token_type", "RefreshToken");
        String jti = UUID.randomUUID().toString();
        claims.put("jti", jti);
        return buildToken(userId, claims, REFRESH_TOKEN_EXPIRATION_TIME);
    }

    private String buildToken(String userId, Map<String, Object> claims, Long expiration) {
        long currentTime = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims != null ? claims : new HashMap<>())
                .subject(userId) // 받는 주체
                .issuer(ISSUER) // 발급자
                .issuedAt(new Date(currentTime)) // 토큰 생성일
                .expiration(new Date(currentTime + expiration)) // 토큰 만료 시간(초)
                .signWith(secretKey, Jwts.SIG.HS256) //  서명 + 알고리즘을 고정하면 JWT 헤더 변조 공격 방지
                .compact();
    }

    // 파싱해주는 메소드
    private static Claims parseToken(String token) {
       return Jwts.parser() // 11 버전까지는 parserBuilder를 통해서 파싱함 -> 12버전은 parser로
               .verifyWith(secretKey)// -> 검증 키 등록
               .build()
               .parseSignedClaims(token) // -> 토큰을 파싱하고 -> 서명 검증까지 해줌
               .getPayload();
    }

    // 유저 아이디(받는 주체) 가져오기
    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }
    // 발급자 가지고 오기
    public String getIssuer(String token) {
        return parseToken(token).getIssuer();
    }
    // 역할 가지고 오기
    public String getMemberType(String token) {
        return parseToken(token).get("role",String.class);
    }

    // 토큰 타입을 가지고 오는 메소드
    public String getTokenType(String token) {
        return parseToken(token).get("token_type",String.class);
    }

    // JTI를 가지고 오는 메소드
    public Optional<String> getJti(String token) {
        return Optional.ofNullable(parseToken(token).get("jti", String.class));
    }


    // AccessToken 검증
    public static Claims verifyAccessToken(String token) {
        // 주석을 추가
        if (token == null || !token.startsWith("Bearer ")) {
            // contains로 하게 되면 중간에 포함된것도 true
            throw new JwtException("토큰 형식이 올바르지 않습니다.");
        }
        // Replace로 하게 되면 Bearer -> 여러개 있을경우 전부 교체 되는 상황이 발생
        token = token.substring(7);
        Claims claims = parseToken(token);

        String userId = claims.getSubject();
        String issuer = claims.getIssuer();

        if (userId == null || userId.isEmpty()) {
            throw new JwtException("아이디가 토큰에 존재하지 않습니다.");
        }
        if (!ISSUER.equals(claims.getIssuer())) {
            throw new JwtException("발급자가 올바르지 않습니다.");
        }
        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            throw new JwtException("토큰이 만료되었습니다.");
        }

        String tokenType = claims.get("token_type", String.class);
        if (!"AccessToken".equals(tokenType)) {
            throw new JwtException("알 수 없는 토큰 타입입니다.");
        }

        return claims;
    }

    // Refresh Token 검증 메소드
    public static Claims verifyRefreshToken(String token) {

        if(token == null || token.isBlank()) {
            throw new JwtException("RefreshToken이 존재하지 않습니다.");
        }

        // Replace로 하게 되면 Bearer -> 여러개 있을경우 전부 교체 되는 상황이 발생

        Claims claims = parseToken(token);

        if (claims.getSubject() == null || claims.getSubject().isEmpty()) {
            throw new JwtException("아이디가 토큰에 존재하지 않습니다.");
        }
        if (!ISSUER.equals(claims.getIssuer())) {
            throw new JwtException("발급자가 올바르지 않습니다.");
        }
        String jti = claims.get("jti", String.class);
        if (jti == null || jti.isEmpty()) {
            throw new JwtException("jti가 올바르지 않습니다.");
        }

        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            throw new JwtException("토큰이 만료되었습니다.");
        }

        String tokenType = claims.get("token_type", String.class);
        if (!"RefreshToken".equals(tokenType)) {
            throw new JwtException("알 수 없는 토큰 타입입니다.");
        }

        return claims;
    }

}

