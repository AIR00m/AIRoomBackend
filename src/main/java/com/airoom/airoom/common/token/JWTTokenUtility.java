package com.airoom.airoom.common.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.JwtTokenizer;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JWTTokenUtility {

    // 환경 변수에 있는 Secret Key 가지고 오기
    public static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY");
    SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    // 컴퓨터는 문자를 이해못함 -> 바이트 배열로 형태로 변환(UTF_8방식으로 변환)
    // 이런 순수 바이트 배열 사용 X -> JJWT 는 HMAC-SHA 알고리즘을 사용해서 알고리즘에 맞는 객체로 반환해준다.
    public static final Long EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(30);
    // 토큰 만료 시간

    public String createJwtToken(String userId, boolean isTeacher){
        long currentTime = System.currentTimeMillis();
        Date expiredTime =new Date(currentTime + EXPIRATION_TIME);
        String role = isTeacher ? "teacher" : "student";

        return Jwts.builder()
                .setSubject(userId) // 받는 주체
                .setIssuer("http://43.200.2.244:8080/airoom") // 발급자
                .setIssuedAt(new Date(currentTime)) // 토큰 생성일
                .setExpiration(expiredTime) // 토큰 만료 시간(초)
                .claim("role", role) // 역할 비표준 클램
                .signWith(secretKey, SignatureAlgorithm.HS256) //  서명
                .compact();

    }



}
