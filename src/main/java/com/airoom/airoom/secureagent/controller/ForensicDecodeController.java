package com.airoom.airoom.secureagent.controller;

import com.airoom.airoom.secureagent.model.dto.StegoDecodeResponse;
import com.airoom.airoom.secureagent.model.service.ForensicDecodeService;
import com.airoom.airoom.secureagent.config.IngestSecurityProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;

@RestController
@RequestMapping("/api/forensic")
//@CrossOrigin(origins="*")
public class ForensicDecodeController {

    private final ForensicDecodeService service;

    public ForensicDecodeController(IngestSecurityProperties sec) {
        this.service = new ForensicDecodeService(sec);
    }

    /** 단일 파일 업로드: PNG/JPEG/PDF만 지원 */
    @PostMapping(value="/decode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoDecodeResponse> decode(@RequestPart("file") MultipartFile file) {
        try {
            var tmp = Files.createTempFile("forensic_", "_" + file.getOriginalFilename()).toFile();
            file.transferTo(tmp);

            StegoDecodeResponse res = service.decode(tmp, file.getOriginalFilename());
            int status = 200;
            if (!res.ok) status = 400;                 // 처리 실패
            else if (!res.hasStego) status = 200;      // 정상 처리했지만 삽입 없음
            return ResponseEntity.status(status).body(res);

        } catch (Exception e) {
            StegoDecodeResponse res = new StegoDecodeResponse();
            res.ok = false;
            res.fileName = file != null ? file.getOriginalFilename() : null;
            res.reason = "server-error";
            return ResponseEntity.status(500).body(res);
        }
    }
}
