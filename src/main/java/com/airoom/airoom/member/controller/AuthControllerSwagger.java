package com.airoom.airoom.member.controller;

import com.airoom.airoom.member.model.dto.LoginRequest;
import com.airoom.airoom.member.model.dto.SignUpRequest;
import com.airoom.airoom.member.model.dto.SignUpResponse;
import com.airoom.airoom.member.model.dto.TokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증/인가 관련 API", description = "로그인 관련 API")
public interface AuthControllerSwagger {
    @Operation(
            summary ="학생 회원가입 API",
            description = "학생 회원가입을 클릭 후 로그인 요청"
    )
    public ResponseEntity<SignUpResponse> enrollStudent(
            @Valid @RequestBody SignUpRequest request
    );
    
    @Operation(
            summary ="선생님 회원가입 API",
            description = "선생님 회원가입을 클릭 후 로그인 요청"
    )
    public ResponseEntity<SignUpResponse> enrollTeacher(
            @Valid @RequestBody SignUpRequest request
    );
    @Operation(
            summary = "선생님/학생 회원가입 API",
            description = "로그인 성공시 교재를 선택하는 페이지로 이동합니다. 토큰 x"
    )
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    );
    @Operation(
            summary = "교재 선택시 토큰 발급 API",
            description = "해당하는 교재를 클릭시 토큰 발급, 토큰에는 클래스룸 번호," +
                    "이름, 클래스룸 학생/선생님 고유번호"
    )
    public ResponseEntity<?> createTokenOnTextbookClick(
            @Valid @RequestBody TokenRequest tokenRequest
    );

    @Operation(
            summary = "로그아웃 메소드",
            description = "프론트에서 access token 삭제 + 백엔드에서 refresh token"
    )
    public ResponseEntity<ResponseCookie> deleteToken(
            @Valid @RequestBody TokenRequest tokenRequest
    );
}
