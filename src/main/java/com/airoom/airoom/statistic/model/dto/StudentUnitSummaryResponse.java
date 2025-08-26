package com.airoom.airoom.statistic.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentUnitSummaryResponse {
    private Long lsTotalProblemsSolved; //총 문제풀이 수
    private Long lsTotalCorrectProblems; //총 정답 수
    private BigDecimal lsAvgAccuracyRate; //평균 정답률
    private String unitTitle; //단원명
    private Integer unitNum; //단원번호

    public StudentUnitSummaryResponse(Long lsTotalProblemsSolved, Long lsTotalCorrectProblems, String unitTitle, Integer unitNum) {
        this.lsTotalProblemsSolved = lsTotalProblemsSolved;
        this.lsTotalCorrectProblems = lsTotalCorrectProblems;
        this.unitTitle = unitTitle;
        this.unitNum = unitNum;
    }
}
