package com.airoom.airoom.notification.model.service;

import com.airoom.airoom.member.model.repository.MemberRepository;
import com.airoom.airoom.notification.model.dto.NotificationDto;
import com.airoom.airoom.notification.model.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmitterService {

    private final EmitterRepository emitterRepository;
    private final MemberRepository memberRepository;

    // keep-alive 전송용 스케줄러 (전역, 데몬스레드로 관리)
//    private static final ScheduledExecutorService  scheduler =
//            Executors.newScheduledThreadPool(1, r -> {
//                Thread t = new Thread(r);
//                t.setDaemon(true);
//                t.setName("sse-keepalive");
//                return t;
//            });

    //이건 화면에서 보내는 연결 신청
    public SseEmitter connectEmitter(Long memberNo) {

        //emitter는 연결 시켜주는 통로

        //emitter객체생성, 생성자를 통해 만료시간 5분 설정
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        //만료시간이 되면 자동으로 브라우저에서 서버에 재연결을 요청

        //사용자별 연결 저장을 위한 사용자 정보 받아서 Map에 주입
        emitterRepository.saveEmitter(memberNo, emitter);
        //emitter가 만료되고 자동으로 재연결해줄때 기존의 emitter는 제거해줘야함

        //SSE 연결이 종료되었을때 서버에서 리소스 정리를 위한 코드 = 생명주기관리
        emitter.onCompletion(() -> {
            emitterRepository.deleteEmitter(memberNo);
            log.info("SSE 연결 완료 - memberNo: {}", memberNo);
        });//정상적으로 종료되었을때

        emitter.onTimeout(() -> {
            emitterRepository.deleteEmitter(memberNo);
            emitter.complete();
            log.info("SSE 연결 타임아웃 - memberNo: {}", memberNo);
        });//시간이 만료됐을때? 근데 재요청한다매

        emitter.onError((e) -> {
            emitterRepository.deleteEmitter(memberNo);
            log.warn("SSE 연결 에러 - memberNo: {}", memberNo, e);
        });//연결중 네트워크 오류 등 에러가 발생했을때

        //heartbit설정하기
        ScheduledExecutorService ses =
                Executors.newSingleThreadScheduledExecutor();

        ScheduledFuture<?> hb = ses.scheduleAtFixedRate(() -> {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("ping")
                                .data("ok")
                                .reconnectTime(3000) // 클라 재연결 지연 제안
                );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, 0, 15, TimeUnit.SECONDS);
        //미전송 알림 재전송

        //emitter생성후 만료시간까지 데이터 안보내면
        // 재연결요청시에 506오류가 뜨니까 더미를 줘야함

        //더미 이벤트 스펙 작성 후 Emitter에 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully"));
        } catch (IllegalArgumentException | IOException e) {
            log.info("SSE 연결 끊김: {}", e.getMessage());
            emitterRepository.deleteEmitter(memberNo); // 등록된 emitter 제거
        }

        //지금은 send()호출시 바로 이벤트를 빌드하는 방법
        //이벤트를 재사용해야할때는 SseEventBuilder에 담아서 재사용하게 구현할수있음
        //SseEmitter.SseEventBuilder event = SseEmitter.event().name().data()

        //이때 작성한 이벤트의 이름은 클라이언트가 이벤트를 불러올때 사용할 수 있음
        //connection이 끊기면 emitter만료


        return emitter; // 🔥 complete() 호출하지 말 것!
    }

    //이건 백엔드에서 보내는 알림 전송 신청
    public void sendNotificationToMember(Long memberNo, NotificationDto notification) {

        log.info("sendNotificationToMember 호출 - memberNo: {}, 알림타입: {}", memberNo, notification.getNotificationType());

        SseEmitter emitter = emitterRepository.getEmitter(memberNo);

        if (emitter != null) {
            log.info("[sendNotificationToMember] Emitter 발견 - memberNo: {}", memberNo);

            try {
                emitter.send(SseEmitter.event()
                        .name("notification")  // 이벤트 타입
                        .data(notification));  // 알림 데이터
            } catch (IllegalArgumentException | IOException e) {
                log.info("SSE 연결 끊김: {}", e.getMessage());
                emitterRepository.deleteEmitter(memberNo); // 등록된 emitter 제거
            }
            log.info("SSE 알림 전송 성공 - memberNo: {}, type: {}",
                    memberNo, notification.getNotificationType());

        } else {
            log.debug("SSE 연결 없음 - memberNo: {}", memberNo);
        }
    }

}
