package com.airoom.airoom.aichat.model;

import com.airoom.airoom.aichat.model.service.CsvIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoIngestOnStart {
    private final CsvIngestService ingest;

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onReady() {
        try {
            ingest.ingestIfEmpty();   // 컬렉션 비어있을 때만 자동 인덱싱
        } catch (Exception e) {
            e.printStackTrace();      // 실패해도 앱은 계속 가동
        }
    }
}
