package com.airoom.airoom.notification.model.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmitterRepository {

    //private final Map<Long, Object> lastEventCache = new ConcurrentHashMap<>();
    //브라우저가 연결이 끊겼다가 다시 연결되었을때 놓친 알림을 재전송하려고 마지막에 보낸 알림을 백업?
    private final Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    //SSE는 이벤트 발생시 전송을 위해 저장해놔야함. SSE는 쉽게 끊기고 생명주기가 짧아서 DB에 저장하기 부적합함
    //네트워크 불안정, 서버 재시작 등으로 끊길수있음
    //Map을 사용하는 것 자체도 서버 재시작시 emitter가 날아가고 서버가 여러대가 되면 Map이 따로 놀게 됨
    //따라서 redis를 사용하는 것이 권장
    //Map으론 현재 연결 관리, 이벤트 데이터는 redis에서 공유할수도있음

    //ConcurrentHashMap은 멀티스레드 환경에서 안전한 동시성 보장
    //=> 동시에 여러 사용자가 같은 자료구조에 연결 요청을 보내므로, 멀티스레드 환경에서 안전하게 접근할 수 있는 Map이 필요
    //ConcurrentHashMap은 락 분할 구조로 되어 있어 성능도 좋고 병렬성도 보장



    //!!!사용자가 여러 탭에서 동시 연결을 시도할때 기존 연결이 강제로 deleteEmitter되는 현상 발생
    //한사람당 하나의 emitter만 생성된다는 뜻
    //    public String makeEmitterId(Long memberNo) {
    //         currentTimeMilis뭐시기 해서 emitter아이디 생성하는 방법
    //    }

    //!!! 사용자가 브라우저 강제종료나 PC가 강제종료되거나 네트워크가 끊어지면 Map에 emitter가 사라지지않고 메모리 누수 문제 발생
    // 1. 하트비트를 통한 연결상태 확인방법 2. 정기적인 죽은 연결 정리 3. 아님 그냥 기존 연결 정리?


    //emitter를 저장하고 기존 연결이 있다면 제거 후 저장
    public void saveEmitter(Long memberNo, SseEmitter emitter) {
        deleteEmitter(memberNo);//emitter 제거 메소드
        //이렇게 하면 여러 탭에서 브라우저를 열면 한쪽 연결이 끊어짐
        sseEmitterMap.put(memberNo,emitter);
    }

    //emitter 제거 메소드
    public void deleteEmitter(Long memberNO) {
        SseEmitter emitter = sseEmitterMap.remove(memberNO);
        if (emitter != null) {
            emitter.complete();
            //연결을 정상적으로 종료시켜주는 메소드
        }


    }
}
