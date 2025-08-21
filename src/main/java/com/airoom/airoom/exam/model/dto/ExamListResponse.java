package com.airoom.airoom.exam.model.dto;

import lombok.*;

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

    public ExamListResponse(Long examNo, String examName, String examStatus, Integer examProblemCount, Long applicantsCount, Long applicantsTotalCount, Integer avgExamScore) {
        this.examNo = examNo;
        this.examName = examName;
        this.examStatus = examStatus;
        this.examProblemCount = examProblemCount;
        this.applicantsCount = applicantsCount;
        this.applicantsTotalCount = applicantsTotalCount;
        this.avgExamScore = avgExamScore;
    }
}
