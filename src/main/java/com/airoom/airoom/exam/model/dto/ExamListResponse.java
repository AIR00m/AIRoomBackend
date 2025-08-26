package com.airoom.airoom.exam.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamListResponse {
    private Long examNo;
    private String examName;
    private String examStatus;
    private Integer examProblemCount;
    private Long applicantsCount;
    private Long applicantsTotalCount;
    private Integer avgExamScore;
    private List<UnitResponse> unitResponseList = new ArrayList<>();
    private LocalDateTime examStartTime;
    private LocalDateTime examEndTime;
    private boolean seIsDone;

    public ExamListResponse(Long examNo, String examName, String examStatus, Integer examProblemCount, Long applicantsCount, Long applicantsTotalCount, Integer avgExamScore, LocalDateTime examStartTime, LocalDateTime examEndTime, boolean seIsDone) {
        this.examNo = examNo;
        this.examName = examName;
        this.examStatus = examStatus;
        this.examProblemCount = examProblemCount;
        this.applicantsCount = applicantsCount;
        this.applicantsTotalCount = applicantsTotalCount;
        this.avgExamScore = avgExamScore;
        this.examStartTime = examStartTime;
        this.examEndTime = examEndTime;
        this.seIsDone = seIsDone;
    }
}
