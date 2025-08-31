package com.airoom.airoom.secureagent.controller;

import com.airoom.airoom.secureagent.config.IngestSecurityProperties;
import com.airoom.airoom.secureagent.model.dto.*;
import com.airoom.airoom.secureagent.model.service.AgentVerifyService;
import com.airoom.airoom.secureagent.model.service.ForensicDecodeService;
import com.airoom.airoom.secureagent.support.CryptoSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@RestController
public class SecureAgentController implements SecureAgentControllerSwagger {

    private final AgentVerifyService agentVerifyService;
    private final IngestSecurityProperties sec;
    private final ForensicDecodeService forensicDecodeService;
    private final KafkaTemplate<String, String> kafka; // nullable
    private static final ObjectMapper M = new ObjectMapper();

    // 다운로드 관련 설정 - final 제거
    @Value("${agent.download.file:}")
    private String filePath;
    @Value("${agent.download.classpath:agent/SecureAgent-2.0.0.exe}")
    private String classpathFile;
    @Value("${agent.download.filename:SecureAgent-2.0.0.exe}")
    private String downloadName;

    private static final String TOPIC_SECURE_LOGS = "secure-agent-logs";
    private static final String TOPIC_SECURE_EVENTS = "secure-agent-events";

    public SecureAgentController(
            AgentVerifyService agentVerifyService,
            IngestSecurityProperties sec,
            @Autowired(required = false) KafkaTemplate<String, String> kafka
    ) {
        this.agentVerifyService = agentVerifyService;
        this.sec = sec;
        this.kafka = kafka;
        this.forensicDecodeService = new ForensicDecodeService(sec);
    }

    // ===== AgentController 기능들 (경로: /api/agent) =====

    /**
     * 에이전트 검증 - GET
     */
    @GetMapping("/api/agent/verify")
    public ResponseEntity<AgentVerifyResponse> verifyGet(
            @RequestParam String version,
            @RequestParam String sha256,
            HttpSession session
    ) {
        return doVerify(version, sha256, session);
    }

    /**
     * 에이전트 검증 - POST
     */
    @PostMapping(value="/api/agent/verify", consumes= MediaType.ALL_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgentVerifyResponse> verifyPost(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String sha256,
            HttpServletRequest request,
            HttpSession session
    ) {
        try {
            if (body != null && !body.isBlank()) {
                try {
                    Map<?,?> m = M.readValue(body, Map.class);
                    if (version == null && m.get("version") != null) version = String.valueOf(m.get("version"));
                    if (sha256  == null && m.get("sha256")  != null) sha256  = String.valueOf(m.get("sha256"));
                } catch (Exception ignore) {}
            }
            if (version == null) version = request.getParameter("version");
            if (sha256  == null) sha256  = request.getParameter("sha256");

            if (version == null || sha256 == null) {
                session.removeAttribute("AGENT_VERIFIED");
                return ResponseEntity.badRequest().body(new AgentVerifyResponse(false, "bad-request"));
            }
            return doVerify(version, sha256, session);

        } catch (Exception e) {
            session.removeAttribute("AGENT_VERIFIED");
            return ResponseEntity.badRequest().body(new AgentVerifyResponse(false, "bad-request"));
        }
    }

    /**
     * 에이전트 오프라인
     */
    @PostMapping("/api/agent/offline")
    public AgentVerifyResponse offline(HttpSession session){
        session.removeAttribute("AGENT_VERIFIED");
        return new AgentVerifyResponse(true, "session-flag-cleared");
    }

    // ===== AgentDownloadController 기능들 (경로: /download) =====

    /**
     * 에이전트 다운로드
     */
    @GetMapping("/download/agent")
    public ResponseEntity<Resource> downloadAgent() {
        ResolveResult rr = resolve();
        HttpHeaders headers = buildDiagHeaders(rr);

        if (rr.resource == null || !rr.resource.exists()) {
            log.warn("[AGENT-DOWNLOAD] NOT FOUND - source={}, path={}, error={}", rr.source, rr.path, rr.error);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).headers(headers).build();
        }

        ContentDisposition cd = ContentDisposition.attachment()
                .filename(downloadName, StandardCharsets.UTF_8)
                .build();

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .headers(headers)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        if (rr.length != null && rr.length >= 0) {
            builder.contentLength(rr.length);
        }

        log.info("[AGENT-DOWNLOAD] OK - source={}, path={}, length={}", rr.source, rr.path, rr.length);
        return builder.body(rr.resource);
    }

    /**
     * 다운로드 디버그 정보
     */
    @GetMapping("/download/agent/debug")
    public Map<String, Object> debug() {
        ResolveResult rr = resolve();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("timestamp", Instant.now().toString());

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("filePath", filePath);
        config.put("classpathFile", classpathFile);
        config.put("downloadName", downloadName);
        config.put("sysprop.agent.download.file", System.getProperty("agent.download.file", ""));
        out.put("config", config);

        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("source", rr.source);
        resolved.put("path", rr.path);
        resolved.put("exists", rr.resource != null && rr.resource.exists());
        resolved.put("readable", rr.resource != null && rr.resource.isReadable());
        resolved.put("length", rr.length);
        resolved.put("error", rr.error);
        out.put("resolved", resolved);

        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("user.dir", System.getProperty("user.dir", ""));
        runtime.put("pwd.abs", Paths.get(".").toAbsolutePath().normalize().toString());
        out.put("runtime", runtime);

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] list = resolver.getResources("classpath*:agent/*");
            List<Map<String, Object>> files = new ArrayList<>();
            for (Resource r : list) {
                Map<String, Object> f = new LinkedHashMap<>();
                f.put("filename", safe(r.getFilename()));
                try {
                    f.put("length", r.contentLength());
                } catch (Exception e) {
                    f.put("length", null);
                }
                try {
                    f.put("url", r.getURL().toString());
                } catch (Exception e) {
                    f.put("url", null);
                }
                files.add(f);
            }
            out.put("classpathList", files);
        } catch (Exception e) {
            out.put("classpathListError", e.toString());
        }

        return out;
    }

    // ===== AgentIngestController 기능들 (경로: /api/agent) =====

    /**
     * 로그 수집
     */
    @PostMapping(value = "/api/agent/log", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Void> ingestLog(@RequestBody String cipher) {
        try {
            String plaintext = CryptoSupport.aesDecryptBase64(cipher, sec.getAesKey());

            ObjectNode json = M.createObjectNode()
                    .put("timestamp", OffsetDateTime.now().toString())
                    .put("level", "INFO")
                    .put("component", "SecureAgent")
                    .put("message", plaintext);

            publish(TOPIC_SECURE_LOGS, M.writeValueAsString(json));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 포렌식 이벤트 수집
     */
    @PostMapping(value = "/api/agent/event", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ingestEvent(@RequestBody ForensicEventRequest req) {
        try {
            if (req.getToken() == null || req.getEncPayload() == null) {
                return ResponseEntity.badRequest().body(Map.of("ok", false, "reason", "bad-request"));
            }

            String payloadJson = CryptoSupport.aesDecryptBase64(req.getEncPayload(), sec.getAesKey());
            JsonNode p = M.readTree(payloadJson);

            String canonical = CryptoSupport.canonical(p);
            String expected  = CryptoSupport.hmacHex(canonical, sec.getTokenSecret(), 12);
            boolean verified = expected.equalsIgnoreCase(req.getToken());

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

    // ===== ForensicDecodeController 기능들 (경로: /api/forensic) =====

    /**
     * 스테가노그래피 디코딩
     */
    @PostMapping(value="/api/forensic/decode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoDecodeResponse> decode(@RequestPart("file") MultipartFile file) {
        try {
            var tmp = Files.createTempFile("forensic_", "_" + file.getOriginalFilename()).toFile();
            file.transferTo(tmp);

            StegoDecodeResponse res = forensicDecodeService.decode(tmp, file.getOriginalFilename());
            int status = 200;
            if (!res.ok) status = 400;
            else if (!res.hasStego) status = 200;
            return ResponseEntity.status(status).body(res);

        } catch (Exception e) {
            StegoDecodeResponse res = new StegoDecodeResponse();
            res.ok = false;
            res.fileName = file != null ? file.getOriginalFilename() : null;
            res.reason = "server-error";
            return ResponseEntity.status(500).body(res);
        }
    }

    // ===== Helper 메서드들 =====

    private ResponseEntity<AgentVerifyResponse> doVerify(String version, String sha256, HttpSession session) {
        boolean ok = agentVerifyService.isAllowed(version, sha256);
        if (ok) {
            session.setAttribute("AGENT_VERIFIED", true);
            return ResponseEntity.ok(new AgentVerifyResponse(true, null));
        }
        session.removeAttribute("AGENT_VERIFIED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AgentVerifyResponse(false, "not-allowed"));
    }

    private void publish(String topic, String value) {
        if (kafka != null) kafka.send(topic, value);
        else log.info("[INGEST] " + topic + " → " + value);
    }

    /** 내부 진단용 결과 객체 */
    private static final class ResolveResult {
        Resource resource;
        String source;   // "file" | "classpath" | "none"
        String path;     // 파일 경로 or 클래스패스 경로
        Long length;     // 알 수 없으면 null
        String error;    // 예외 메시지 등
    }

    private ResolveResult resolve() {
        ResolveResult rr = new ResolveResult();
        rr.source = "none";

        // 1) 외부 파일 경로 우선
        try {
            if (filePath != null && !filePath.isBlank()) {
                Path p = Paths.get(filePath);
                rr.path = p.toString();
                if (Files.exists(p)) {
                    rr.source = "file";
                    rr.resource = new FileSystemResource(p);
                    rr.length = safeLength(rr.resource);
                    return rr;
                } else {
                    log.debug("[AGENT-RESOLVE] external file NOT exists: {}", p);
                }
            }
        } catch (Exception e) {
            rr.error = "file-check: " + e.toString();
            log.warn("[AGENT-RESOLVE] external file error: {}", e.toString());
        }

        // 2) 클래스패스
        try {
            rr.path = classpathFile;
            ClassPathResource cpr = new ClassPathResource(classpathFile);
            if (cpr.exists()) {
                rr.source = "classpath";
                rr.resource = cpr;
                rr.length = safeLength(cpr);
                return rr;
            } else {
                log.debug("[AGENT-RESOLVE] classpath NOT exists: {}", classpathFile);
            }
        } catch (Exception e) {
            rr.error = (rr.error == null ? "" : rr.error + " | ") + "classpath-check: " + e.toString();
            log.warn("[AGENT-RESOLVE] classpath error: {}", e.toString());
        }

        // 둘 다 실패
        if (rr.error == null) rr.error = "not-found";
        return rr;
    }

    private HttpHeaders buildDiagHeaders(ResolveResult rr) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Agent-Resolved-Source", asciiOrFallback(rr.source));
        h.add("X-Agent-Resolved-Path-Enc", urlEncodeUtf8(rr.path));
        if (isAscii(rr.path)) {
            h.add("X-Agent-Resolved-Path", rr.path);
        }
        h.add("X-Agent-Resolved-Path-B64", base64Utf8(rr.path));
        h.add("X-Agent-Error", asciiOrFallback(rr.error));
        h.add("X-Agent-Length", rr.length == null ? "-1" : String.valueOf(rr.length));
        return h;
    }

    private boolean isAscii(String s) {
        if (s == null) return true;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 0x7F) return false;
        }
        return true;
    }

    private String asciiOrFallback(String s) {
        if (s == null) return "";
        if (isAscii(s)) return s;
        return "(non-ascii)";
    }

    private String urlEncodeUtf8(String s) {
        try {
            return s == null ? "" : java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private String base64Utf8(String s) {
        if (s == null) return "";
        return java.util.Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private Long safeLength(Resource r) {
        try {
            long len = r.contentLength();
            return (len >= 0 ? len : null);
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String s) {
        return (s == null ? "" : s);
    }
}
