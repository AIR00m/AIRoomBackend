package com.airoom.airoom.secureagent.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.*;

@RestController
@RequestMapping("/download")
public class AgentDownloadController {

    // 개발: 외부 절대경로로 내려줌 (우선순위 1)
    @Value("${agent.download.file:}")
    private String filePath;

    // 운영: 클래스패스에 포함된 바이너리로 내려줌 (우선순위 2)
    @Value("${agent.download.classpath:agent/SecureAgent-0.9.1.exe}")
    private String classpathFile;

    // 노출 파일명
    @Value("${agent.download.filename:SecureAgent-0.9.1.exe}")
    private String downloadName;

    @GetMapping("/agent")
    public ResponseEntity<Resource> downloadAgent() {
        Resource res = resolveResource();
        if (res == null || !res.exists()) {
            return ResponseEntity.notFound().build();
        }

        long length = getContentLength(res);
        ContentDisposition cd = ContentDisposition.attachment().filename(downloadName).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(length >= 0 ? length : -1)
                .body(res);
    }

    private Resource resolveResource() {
        try {
            if (filePath != null && !filePath.isBlank()) {
                Path p = Paths.get(filePath);
                if (Files.exists(p)) return new FileSystemResource(p);
            }
            ClassPathResource cpr = new ClassPathResource(classpathFile);
            return cpr.exists() ? cpr : null;
        } catch (Exception e) {
            return null;
        }
    }

    private long getContentLength(Resource r) {
        try { return r.contentLength(); } catch (Exception ignore) { return -1L; }
    }
}
