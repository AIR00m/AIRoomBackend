package com.airoom.airoom.attach.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentDto {
    private String originalName;
    private String savedName;
    private String s3Key;
}
