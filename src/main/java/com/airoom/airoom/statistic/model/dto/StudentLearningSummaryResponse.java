package com.airoom.airoom.statistic.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentLearningSummaryResponse {
    private Long lsTotalLearningDays; //총 학습일
    private Long lsTotalLearningTime; //총 학습시간(밀리초)
    private Long lsTotalProblemsSolved; //총 문제풀이 수
    private Long lsTotalCorrectProblems; //총 정답 수
    private BigDecimal lsAvgAccuracyRate; //평균 정답률

    public StudentLearningSummaryResponse(Long lsTotalLearningDays, Long lsTotalLearningTime, Long lsTotalProblemsSolved, Long lsTotalCorrectProblems) {
        this.lsTotalLearningDays = lsTotalLearningDays;
        this.lsTotalLearningTime = lsTotalLearningTime;
        this.lsTotalProblemsSolved = lsTotalProblemsSolved;
        this.lsTotalCorrectProblems = lsTotalCorrectProblems;
    }

    public void calc(Long addDays, Long addTime, Long addSolved, Long addCorrect) {
        if (this.lsTotalLearningDays == null) this.lsTotalLearningDays = 0L;
        if (this.lsTotalLearningTime == null) this.lsTotalLearningTime = 0L;
        if (this.lsTotalProblemsSolved == null) this.lsTotalProblemsSolved = 0L;
        if (this.lsTotalCorrectProblems == null) this.lsTotalCorrectProblems = 0L;

        this.lsTotalLearningDays += addDays;
        this.lsTotalLearningTime += addTime;
        this.lsTotalProblemsSolved += addSolved;
        this.lsTotalCorrectProblems += addCorrect;

        if (this.lsTotalProblemsSolved > 0) {
            this.lsAvgAccuracyRate = BigDecimal.valueOf(this.lsTotalCorrectProblems * 100.0 / this.lsTotalProblemsSolved)
                    .setScale(2, RoundingMode.HALF_UP); // 소수점 2자리 반올림
        } else {
            this.lsAvgAccuracyRate = BigDecimal.ZERO;
        }
    }
}
