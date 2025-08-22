package com.airoom.airoom.attach.model.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresignedUrlResponse {
    private String presignedUrl;
    private String originalName;
    private String savedName;
    private String s3Key;
}
