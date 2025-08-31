package com.airoom.airoom.secureagent.controller;

import com.airoom.airoom.secureagent.model.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "SecureAgent API", description = "보안 에이전트 관련 API")
public interface SecureAgentControllerSwagger {

    @Operation(
            summary = "에이전트 검증 API (GET)",
            description = "에이전트의 버전과 SHA256 해시값을 검증합니다. (GET 방식)"
    )
    public ResponseEntity<AgentVerifyResponse> verifyGet(
            @Parameter(description = "에이전트 버전", required = true)
            @RequestParam String version,
            @Parameter(description = "에이전트 SHA256 해시값", required = true)
            @RequestParam String sha256,
            HttpSession session
    );

    @Operation(
            summary = "에이전트 검증 API (POST)",
            description = "에이전트의 버전과 SHA256 해시값을 검증합니다. (POST 방식, JSON 또는 Form 데이터 지원)"
    )
    public ResponseEntity<AgentVerifyResponse> verifyPost(
            @Parameter(description = "요청 본문 (JSON 형태)", required = false)
            @RequestBody(required = false) String body,
            @Parameter(description = "에이전트 버전", required = false)
            @RequestParam(required = false) String version,
            @Parameter(description = "에이전트 SHA256 해시값", required = false)
            @RequestParam(required = false) String sha256,
            HttpServletRequest request,
            HttpSession session
    );

    @Operation(
            summary = "에이전트 오프라인 처리 API",
            description = "에이전트를 오프라인 상태로 전환하고 세션을 정리합니다."
    )
    public AgentVerifyResponse offline(HttpSession session);

    @Operation(
            summary = "에이전트 다운로드 API",
            description = "보안 에이전트 실행파일을 다운로드합니다."
    )
    public ResponseEntity<Resource> downloadAgent();

    @Operation(
            summary = "다운로드 디버그 정보 API",
            description = "에이전트 다운로드 관련 설정 및 상태 정보를 조회합니다."
    )
    public Map<String, Object> debug();

    @Operation(
            summary = "에이전트 로그 수집 API",
            description = "에이전트에서 전송하는 암호화된 로그를 수집합니다."
    )
    public ResponseEntity<Void> ingestLog(
            @Parameter(description = "AES 암호화된 로그 데이터 (Base64)", required = true)
            @RequestBody String cipher
    );

    @Operation(
            summary = "포렌식 이벤트 수집 API",
            description = "에이전트에서 전송하는 포렌식 이벤트 데이터를 수집하고 검증합니다."
    )
    public ResponseEntity<Map<String, Object>> ingestEvent(
            @Parameter(description = "포렌식 이벤트 요청 데이터", required = true)
            @RequestBody ForensicEventRequest req
    );

    @Operation(
            summary = "스테가노그래피 디코딩 API",
            description = "이미지나 PDF 파일에 숨겨진 포렌식 데이터를 추출합니다. PNG, JPEG, PDF 형식을 지원합니다."
    )
    public ResponseEntity<StegoDecodeResponse> decode(
            @Parameter(description = "분석할 파일 (PNG, JPEG, PDF)", required = true)
            @RequestPart("file") MultipartFile file
    );
}
