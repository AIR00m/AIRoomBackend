package com.airoom.airoom.notification.controller;

import com.airoom.airoom.notification.model.repository.EmitterRepository;
import com.airoom.airoom.notification.model.service.EmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final EmitterService emitterService;

    @GetMapping(value = "/emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    //헤더에 text/event-stream을 포함하는 경우에만 수신하겠다
    public SseEmitter connect(@RequestParam Long memberNo){

        return emitterService.connectEmitter(memberNo);
        //JWT 엑세스 토큰을 보내주니까 Userdetail로 식별
    }
}
