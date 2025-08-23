package com.airoom.airoom.secureagent.controller;

import com.airoom.airoom.secureagent.config.IngestSecurityProperties;
import com.airoom.airoom.secureagent.model.dto.ForensicEventRequest;
import com.airoom.airoom.secureagent.support.CryptoSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin("*")
public class AgentIngestController {

    private static final String TOPIC_SECURE_LOGS   = "secure-agent-logs";
    private static final String TOPIC_SECURE_EVENTS = "secure-agent-events";

    private final IngestSecurityProperties sec;
    private final ObjectMapper M = new ObjectMapper();
    private final KafkaTemplate<String, String> kafka; // nullable

    public AgentIngestController(IngestSecurityProperties sec,
                                 @Autowired(required = false) KafkaTemplate<String, String> kafka) {
        this.sec = sec;
        this.kafka = kafka;
    }

    /** 에이전트 라인 로그 수신 (본문은 AES+Base64) → JSON 래핑 후 카프카 */
    @PostMapping(value = "/log", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Void> ingestLog(@RequestBody String cipher) {
        try {
            String plaintext = CryptoSupport.aesDecryptBase64(cipher, sec.getAesKey());

            ObjectNode json = M.createObjectNode()
                    .put("timestamp", OffsetDateTime.now().toString()) // ISO8601
                    .put("level", "INFO")
                    .put("component", "SecureAgent")
                    .put("message", plaintext);

            publish(TOPIC_SECURE_LOGS, M.writeValueAsString(json));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** 포렌식 이벤트 수신 (encPayload만 암호문) → 복호화/검증 후 카프카(JSON) */
    @PostMapping(value = "/event", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ingestEvent(@RequestBody ForensicEventRequest req) {
        try {
            if (req.getToken() == null || req.getEncPayload() == null) {
                return ResponseEntity.badRequest().body(Map.of("ok", false, "reason", "bad-request"));
            }

            // 1) encPayload 복호화 → ForensicPayload JSON
            String payloadJson = CryptoSupport.aesDecryptBase64(req.getEncPayload(), sec.getAesKey());
            JsonNode p = M.readTree(payloadJson); // p.ts, p.uid, p.deviceId, p.action, ...

            // 2) canonical + HMAC 검증
            String canonical = CryptoSupport.canonical(p);
            String expected  = CryptoSupport.hmacHex(canonical, sec.getTokenSecret(), 12);
            boolean verified = expected.equalsIgnoreCase(req.getToken());

            // 3) 카프카 적재 (enriched JSON)
            ObjectNode enriched = (ObjectNode) p.deepCopy();
            enriched.put("token", req.getToken());
            enriched.put("verified", verified);
            if (req.getAgentTs()  != null) enriched.put("agentTs",  req.getAgentTs());
            if (req.getAgentVer() != null) enriched.put("agentVer", req.getAgentVer());
            enriched.put("receivedAt", OffsetDateTime.now().toString());

            publish(TOPIC_SECURE_EVENTS, M.writeValueAsString(enriched));

            Map<String,Object> res = new LinkedHashMap<>();
            res.put("ok", verified);
            return verified ? ResponseEntity.ok(res)
                    : ResponseEntity.status(400).body(res);

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("ok", false, "reason", "bad-payload"));
        }
    }

    private void publish(String topic, String value) {
        if (kafka != null) kafka.send(topic, value);
        else System.out.println("[INGEST] " + topic + " → " + value);
    }
}
