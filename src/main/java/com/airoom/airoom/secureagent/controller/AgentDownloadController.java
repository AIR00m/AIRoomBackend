package com.airoom.airoom.secureagent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.net.URI;

@RestController
@RequestMapping("/download")
public class AgentDownloadController {

    private static final Logger log = LoggerFactory.getLogger(AgentDownloadController.class);

    // 개발: 외부 절대경로로 내려줌 (우선순위 1)
    @Value("${agent.download.file:}")
    private String filePath;

    // 운영: 클래스패스에 포함된 바이너리로 내려줌 (우선순위 2)
    @Value("${agent.download.classpath:agent/보안지킴이-2.0.0.exe}")
    private String classpathFile;

    // 노출 파일명
    @Value("${agent.download.filename:보안지킴이-2.0.0.exe}")
    private String downloadName;

    /** 내부 진단용 결과 객체 */
    private static final class ResolveResult {
        Resource resource;
        String source;   // "file" | "classpath" | "none"
        String path;     // 파일 경로 or 클래스패스 경로
        Long length;     // 알 수 없으면 null
        String error;    // 예외 메시지 등
    }

    /** 실제 다운로드 (GET) */
    @GetMapping("/agent")
    public ResponseEntity<Resource> downloadAgent() {
        ResolveResult rr = resolve();

        HttpHeaders headers = buildDiagHeaders(rr);

        if (rr.resource == null || !rr.resource.exists()) {
            log.warn("[AGENT-DOWNLOAD] NOT FOUND - source={}, path={}, error={}", rr.source, rr.path, rr.error);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).headers(headers).build();
        }

        // 파일 이름(한글 안전)
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(downloadName, StandardCharsets.UTF_8)
                .build();

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .headers(headers)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        if (rr.length != null && rr.length >= 0) {
            builder.contentLength(rr.length); // 알면 설정, 모르면 생략(Chunked)
        }

        log.info("[AGENT-DOWNLOAD] OK - source={}, path={}, length={}", rr.source, rr.path, rr.length);
        return builder.body(rr.resource);
    }

    /** 상세 디버그 JSON (설정/클래스패스 목록/실시간 경로 등) */
    @GetMapping("/agent/debug")
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

        // 클래스패스 'agent/' 폴더 안에 뭐가 들어있는지 나열
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


    /** 리소스 결정 (외부 파일 경로 > 클래스패스) + 상세 로깅 */
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
        // 한글/공백 등은 URL 인코딩 헤더로만 노출 (ASCII 보장)
        h.add("X-Agent-Resolved-Path-Enc", urlEncodeUtf8(rr.path));
        // 원문 경로는 ASCII일 때만 그대로 노출
        if (isAscii(rr.path)) {
            h.add("X-Agent-Resolved-Path", rr.path);
        }
        // 원문을 꼭 보고 싶으면 BASE64도 함께 제공 (역변환 쉬움)
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
        // ASCII 아닐 때 간단한 표시만
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
