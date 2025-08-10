package com.airoom.airoom.notification.model.service;

import com.airoom.airoom.notification.model.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmitterService {

//    private final RedisTemplate<String, Object> redisTemplate;
    private final EmitterRepository emitterRepository;

    public SseEmitter connectEmitter(Long memberNo) {

        //emitter는 연결 시켜주는 통로

        //emitter객체생성, 생성자를 통해 만료시간 1시간 설정
        SseEmitter emitter = new SseEmitter(60*60*1000L);
        //만료시간이 되면 자동으로 브라우저에서 서버에 재연결을 요청
        
        //사용자별 연결 저장을 위한 사용자 정보 받아서 Map에 주입
        emitterRepository.saveEmitter(memberNo,emitter);
        //emitter가 만료되고 자동으로 재연결해줄때 기존의 emitter는 제거해줘야함

        //SSE 연결이 종료되었을때 서버에서 리소스 정리를 위한 코드
        emitter.onCompletion(() -> emitterRepository.deleteEmitter(memberNo));//정상적으로 종료되었을때

        emitter.onTimeout(() -> emitterRepository.deleteEmitter(memberNo));//시간이 만료됐을때? 근데 재요청한다매

        emitter.onError((e) -> emitterRepository.deleteEmitter(memberNo));//연결중 네트워크 오류 등 에러가 발생했을때


        //미전송 알림 재전송

        //emitter생성후 만료시간까지 데이터 안보내면
        // 재연결요청시에 506오류가 뜨니까 더미를 줘야함

        try {
            //더미 이벤트 스펙 작성 후 Emitter에 전송
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully"));
            
            //지금은 send()호출시 바로 이벤트를 빌드하는 방법
            //이벤트를 재사용해야할때는 SseEventBuilder에 담아서 재사용하게 구현할수있음
            //SseEmitter.SseEventBuilder event = SseEmitter.event().name().data()
            
            //이때 작성한 이벤트의 이름은 클라이언트가 이벤트를 불러올때 사용할 수 있음
            //connection이 끊기면 emitter만료
        } catch (IOException e) {
            emitterRepository.deleteEmitter(memberNo); // emitter 정리
            log.warn("연결 전송 실패", e);
        }

       return emitter;
    }

}
