package com.airoom.airoom.statistic.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
/**
 * 학습 요약(학생별, 우리반) DTO
 */
public class StudentLearningSummaryResponse {
    private Long lsTotalLearningDays; //총 학습일(필수)
    private Long lsTotalLearningTime; //총 학습시간(밀리초)(필수)
    private Long lsTotalProblemsSolved; //총 문제풀이 수
    private Long lsTotalCorrectProblems; //총 정답 수
    private BigDecimal lsAvgAccuracyRate; //평균 정답률(필수)

    public StudentLearningSummaryResponse(Long lsTotalLearningDays, Long lsTotalLearningTime, Long lsTotalProblemsSolved, Long lsTotalCorrectProblems) {
        this.lsTotalLearningDays = lsTotalLearningDays;
        this.lsTotalLearningTime = lsTotalLearningTime;
        this.lsTotalProblemsSolved = lsTotalProblemsSolved;
        this.lsTotalCorrectProblems = lsTotalCorrectProblems;
    }

    public void calcAvgAccuracyRate() {
        if (this.lsTotalProblemsSolved > 0) {
            this.lsAvgAccuracyRate = BigDecimal.valueOf(this.lsTotalCorrectProblems * 100.0 / this.lsTotalProblemsSolved)
                    .setScale(2, RoundingMode.HALF_UP); // 소수점 2자리 반올림
        } else {
            this.lsAvgAccuracyRate = BigDecimal.ZERO;
        }
    }
}
