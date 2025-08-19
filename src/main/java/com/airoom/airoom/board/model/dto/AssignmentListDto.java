package com.airoom.airoom.board.model.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentListDto {
    private Long id;
    private String title;
    private boolean isGroupAssignment;
    private String startDate;
    private String dueDate;
    private String submitStatus; // "true" or "false"
}