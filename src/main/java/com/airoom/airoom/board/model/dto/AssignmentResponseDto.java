package com.airoom.airoom.board.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponseDto {
    private Long id;
    private String assignBoardTitle;
    private String subject;
    private String boardContent;
    private String status; // "ongoing", "completed"
    private Boolean isGroupAssignment;

    // 진행률 정보 (교사용)
    private Integer progress;
    private Integer participants;

    // 날짜 정보
    private String assignStart;
    private String assignEnd;
    private String dueDate; // assignEnd와 동일

    // 학생용 정보
    private String studentStatus; // "not-started", "in-progress", "completed"
    private Integer studentScore;

    // 완료된 과제 정보
    private Integer completionRate;
    private Integer averageScore;
    private String completedDate;

    // 첨부파일 정보
    private Boolean hasAttachment;
    private Integer attachmentCount;
}