package com.airoom.airoom.member.controller;

import com.airoom.airoom.common.token.CookieUtility;
import com.airoom.airoom.common.token.JWTTokenUtility;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.dto.LoginRequest;
import com.airoom.airoom.member.model.dto.SignUpRequest;
import com.airoom.airoom.member.model.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JWTTokenUtility jwtUtility;
    private final CookieUtility cookieUtility;

      //학생 회원가입
     @PostMapping("/signup/student")
     public ResponseEntity<?> enrollStudent(@RequestBody SignUpRequest request) {
        Member member;
        return null;
     }

    // 선생님 회원가입
    @PostMapping("/signup/teacher")
    public ResponseEntity<?> enrollTeacher(@RequestBody SignUpRequest dto){
        return null;
    }

    // 1. 로그인 요청
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
         // 2. DB에 접근하여 로그인 정보 확인
         Member member = authService.authenticate(request.id(), request.pwd());
         boolean isTeacher = member.getMemberType().toString().equals("TEACHER");

         // 3. 서버에서 토큰을 발급
         String accessToken = jwtUtility.createAccessToken(request.id(), isTeacher);
         String refreshToken = jwtUtility.createRefreshToken(request.id(), isTeacher);

         //3-1 Redis에는 (Time To Live)기능이 존재하여

        ResponseCookie cookie = cookieUtility.refreshTokenCookie(refreshToken);
        // 쿠키에 담는 것도 좋지만 프론트에서 localstorage에 담는것도 생각해보는 것을 추천

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("Access_Token", accessToken, "role", member.getMemberType().name()));

    }







}
