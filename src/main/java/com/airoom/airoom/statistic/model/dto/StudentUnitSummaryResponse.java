package com.airoom.airoom.statistic.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
/**
 * 단원별 성취현황(우리반, 학생별) DTO
 */
public class StudentUnitSummaryResponse {
    private Long lsTotalProblemsSolved; //총 문제풀이 수
    private Long lsTotalCorrectProblems; //총 정답 수
    private BigDecimal lsAvgAccuracyRate; //평균 정답률(필수)
    private String unitTitle; //단원명(필수)
    private Integer unitNum; //단원번호(필수)

    public StudentUnitSummaryResponse(Long lsTotalProblemsSolved, Long lsTotalCorrectProblems, String unitTitle, Integer unitNum) {
        this.lsTotalProblemsSolved = lsTotalProblemsSolved;
        this.lsTotalCorrectProblems = lsTotalCorrectProblems;
        this.unitTitle = unitTitle;
        this.unitNum = unitNum;
    }
}
