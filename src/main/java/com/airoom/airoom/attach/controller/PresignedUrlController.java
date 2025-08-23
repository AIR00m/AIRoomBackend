package com.airoom.airoom.attach.controller;

import com.airoom.airoom.attach.model.dto.*;
import com.airoom.airoom.attach.model.service.AttachmentService;
import com.airoom.airoom.attach.model.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/presigned-url")
public class PresignedUrlController implements PresignedUrlSwagger {

    private final PresignedUrlService presignedUrlService;
    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    public ResponseEntity<PresignedUrlResponse> getUploadPresignedUrl(@RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(presignedUrlService.generateUploadUrl(request));
    }

    @PostMapping("/attachment")
    public ResponseEntity<Void> saveAttachment(@RequestBody AttachmentRequest request) {
        attachmentService.saveAttachment(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/download")
    public ResponseEntity<String> getDownloadPresignedUrl(@RequestBody DownloadUrlRequest request) {
        return ResponseEntity.ok(presignedUrlService.generateDownloadUrl(request.getS3Key()));
    }
}
