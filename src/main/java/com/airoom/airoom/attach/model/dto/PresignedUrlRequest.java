package com.airoom.airoom.attach.model.dto;

import com.airoom.airoom.board.BoardType;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresignedUrlRequest {
    private Long boardNo;
    private String originalName;
    private BoardType boardType;
}
