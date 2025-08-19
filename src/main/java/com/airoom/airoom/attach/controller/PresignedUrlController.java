package com.airoom.airoom.attach.controller;

import com.airoom.airoom.attach.model.dto.PresignedUrlRequest;
import com.airoom.airoom.attach.model.dto.PresignedUrlResponse;
import com.airoom.airoom.attach.model.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/presigned-url")
public class PresignedUrlController implements PresignedUrlSwagger{

    private final PresignedUrlService presignedUrlService;

    @PostMapping
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(@RequestBody PresignedUrlRequest request){
        return ResponseEntity.ok(presignedUrlService.generateUploadUrl(request));
    }
}
