package com.airoom.airoom.attach.controller;

import com.airoom.airoom.attach.model.dto.AttachmentDto;
import com.airoom.airoom.attach.model.dto.PresignedUrlRequest;
import com.airoom.airoom.attach.model.dto.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "PresignedUrl 관련 API", description = "PresignedUrl 관련 API")
public interface PresignedUrlSwagger {

    @Operation(
            summary = "업로드용 PresignedUrl API",
            description = "업로드용 PresignedUrl을 생성합니다."
    )
    public ResponseEntity<PresignedUrlResponse> getUploadPresignedUrl(
            @RequestBody PresignedUrlRequest request
    );

    @Operation(
            summary = "첨부파일 등록 API",
            description = "첨부파일을 등록합니다."
    )
    public ResponseEntity<Void> saveAttachment(@RequestBody AttachmentDto dto);

    @Operation(
            summary = "다운로드용 PresignedUrl API",
            description = "다운로드용 PresignedUrl을 생성합니다."
    )
    public ResponseEntity<String> getDownloadPresignedUrl(@RequestBody String S3Key);
}
