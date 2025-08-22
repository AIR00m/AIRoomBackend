package com.airoom.airoom.attach.model.dto;

import com.airoom.airoom.board.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentResponse {
    private Long attachNo;
    private Long boardNo;
    private BoardType boardType;
    private String originalName;
    private String savedName;
    private String s3Key;
}
