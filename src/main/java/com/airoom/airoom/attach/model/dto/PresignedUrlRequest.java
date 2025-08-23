package com.airoom.airoom.attach.model.dto;

import com.airoom.airoom.board.entity.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresignedUrlRequest {
    private Long boardNo;
    private String originalName;
    private BoardType boardType;
}
