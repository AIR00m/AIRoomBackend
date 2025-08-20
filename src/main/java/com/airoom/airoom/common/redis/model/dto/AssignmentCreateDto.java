package com.airoom.airoom.common.redis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentCreateDto {
    private String boardContent;
    private String boardType; // "ASSIGN"
    private Long memberNo;
    private Long classroomNo;
    private String assignBoardTitle;
    private List<Long> targetNo;
}