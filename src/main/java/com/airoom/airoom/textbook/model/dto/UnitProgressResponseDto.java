package com.airoom.airoom.textbook.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitProgressResponseDto {
    private Long unitNo;
    private String unitTitle;
    private Integer unitNum;        // 추가: 단원번호
    private Integer progressLastPage;
    private LocalDateTime updatedAt;
}