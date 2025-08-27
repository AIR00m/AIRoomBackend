package com.airoom.airoom.chat.model.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrollRequest {
    private Long crNo;
    private Long beforeId;
}
