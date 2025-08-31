package com.airoom.airoom.aichat.controller;

import com.airoom.airoom.aichat.model.dto.*;
import com.airoom.airoom.common.token.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "AI Chatbot API", description = "AI 챗봇 및 채팅방 관리 관련 API")
public interface AiChatbotControllerSwagger {

    @Operation(
            summary = "AI 질문하기 API",
            description = "AI 챗봇에게 질문을 전송하고 답변을 받습니다. 채팅방 ID가 있으면 대화 기록이 저장되며, 없으면 단순 RAG 검색만 수행합니다."
    )
    public ResponseEntity<AskResponse> ask(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @Parameter(description = "AI 질문 요청 데이터", required = true)
            @RequestBody AskRequest req
    );

    @Operation(
            summary = "채팅방 생성 API",
            description = "새로운 AI 채팅방을 생성합니다."
    )
    public ResponseEntity<ChatDto.RoomRes> createRoom();

    @Operation(
            summary = "채팅방 목록 조회 API",
            description = "현재 사용자의 모든 AI 채팅방 목록을 최근 질문 시간 순으로 조회합니다."
    )
    public ResponseEntity<List<ChatDto.RoomRes>> listRooms();

    @Operation(
            summary = "채팅방 삭제 API",
            description = "지정된 AI 채팅방과 관련된 모든 메시지를 삭제합니다."
    )
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "삭제할 채팅방 ID", required = true)
            @PathVariable Long roomId
    );

    @Operation(
            summary = "채팅방 메시지 조회 API",
            description = "지정된 채팅방의 메시지 목록을 조회합니다. 페이지네이션을 지원하며, beforeId 기준으로 이전 메시지들을 가져옵니다."
    )
    public ResponseEntity<List<ChatDto.MsgRes>> getMessages(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable Long roomId,
            @Parameter(description = "이 ID보다 이전 메시지들을 조회 (페이지네이션용)", required = false)
            @RequestParam(required = false) Long beforeId,
            @Parameter(description = "조회할 메시지 개수 (기본값: 30, 최대: 100)", required = false)
            @RequestParam(defaultValue = "30") int limit,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomUserDetails me
    );

    @Operation(
            summary = "시스템 상태 체크 API",
            description = "OpenAI와 Qdrant 서비스의 연결 상태를 확인합니다."
    )
    public Map<String, String> health();

    @Operation(
            summary = "Qdrant 데이터 개수 조회 API",
            description = "Qdrant 벡터 데이터베이스에 저장된 문서의 개수를 조회합니다."
    )
    public Map<String, Object> count();

    @Operation(
            summary = "벡터 검색 테스트 API",
            description = "RAG 처리 없이 순수 벡터 임베딩과 검색만 테스트합니다. 개발 및 디버깅 목적으로 사용됩니다."
    )
    public List<SourceDto> drySearch(
            @Parameter(description = "검색할 질문 텍스트", required = true)
            @RequestParam String q,
            @Parameter(description = "반환할 검색 결과 개수 (기본값: 5)", required = false)
            @RequestParam(defaultValue = "5") int k
    );

    @Operation(
            summary = "사용자 컨텍스트 조회 API",
            description = "현재 로그인한 사용자의 학습 컨텍스트 정보를 조회합니다. AI 답변 개인화에 사용되는 데이터입니다."
    )
    public Map<String, Object> context(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomUserDetails me
    );
}
