package com.airoom.airoom.notification.controller;

import com.airoom.airoom.member.model.service.MemberService;
import com.airoom.airoom.notification.model.service.EmitterService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController implements SseControllerSwagger {

    private final EmitterService emitterService;
    private final MemberService memberService;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(@RequestParam("memberId") String memberId, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no"); // nginx 버퍼링 방지
        try {
            // memberId로 실제 memberNo 조회
            Long memberNo = memberService.findMemberNoByMemberId(memberId);

            if (memberNo == null) {
                log.error("사용자를 찾을 수 없습니다: {}", memberId);
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다: " + memberId
                );
            }

            return ResponseEntity.ok(emitterService.connectEmitter(memberNo));

        } catch (Exception e) {
            log.error("SSE 연결 중 오류 발생 - memberId: {}", memberId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SSE 연결 실패: " + e.getMessage()
            );
        }
    }

}