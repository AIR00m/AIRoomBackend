package com.airoom.airoom.notification.controller;

import com.airoom.airoom.member.model.service.MemberService;
import com.airoom.airoom.notification.model.service.EmitterService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController{

    private final EmitterService emitterService;
    private final MemberService memberService;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(
            @RequestParam("memberId") String memberId,
            @RequestParam(value = "lastEventId", required = false) Long lastEventIdParam,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventIdHeader,
            HttpServletResponse response
    ) {
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");

        try {
            Long memberNo = memberService.findMemberNoByMemberId(memberId);
            if (memberNo == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + memberId);
            }

            Long lastEventId = resolveLastEventId(lastEventIdParam, lastEventIdHeader);
            return ResponseEntity.ok(emitterService.connectEmitter(memberNo, lastEventId));

        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("SSE 연결 중 오류 - memberId: {}", memberId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SSE 연결 실패: " + e.getMessage());
        }
    }

    private Long resolveLastEventId(Long param, String header) {
        if (param != null) return param;
        if (header == null || header.isBlank()) return null;
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
