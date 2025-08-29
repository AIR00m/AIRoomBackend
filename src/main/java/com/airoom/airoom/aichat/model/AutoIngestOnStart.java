package com.airoom.airoom.aichat.model;

import com.airoom.airoom.aichat.model.service.CsvIngestService;
import com.airoom.airoom.aichat.model.service.QdrantClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoIngestOnStart {
    private final QdrantClient qdrant;
    private final CsvIngestService ingest;

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onReady() {
        try {
            if (qdrant.count() == 0) {   // 컬렉션이 비어있으면
                ingest.ingest();         // 자동 인덱싱
            }
        } catch (Exception e) {
            // 로그만 남기고 앱은 계속 가동
            e.printStackTrace();
        }
    }
}
