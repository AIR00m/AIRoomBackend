package com.airoom.airoom.statistic.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentUnitSummaryDetailResponse {
    private String usClassroomStudentName; //클래스룸 학생이름(필수)
    private Long usTotalProblemsSolved; //총 문제풀이 수
    private Long usTotalCorrectProblems; //총 정답 수
    private BigDecimal usAvgAccuracyRate; //평균 정답률(필수)
    private String unitTitle; //단원명(필수)
    private Integer unitNum; //단원번호(필수)

    public StudentUnitSummaryDetailResponse(String usClassroomStudentName, Long usTotalProblemsSolved, Long usTotalCorrectProblems, String unitTitle, Integer unitNum) {
        this.usClassroomStudentName = usClassroomStudentName;
        this.usTotalProblemsSolved = usTotalProblemsSolved;
        this.usTotalCorrectProblems = usTotalCorrectProblems;
        this.unitTitle = unitTitle;
        this.unitNum = unitNum;
    }

    public void calcAvgAccuracyRate() {
        if (this.usTotalProblemsSolved > 0) {
            this.usAvgAccuracyRate = BigDecimal.valueOf(this.usTotalCorrectProblems * 100.0 / this.usTotalProblemsSolved)
                    .setScale(2, RoundingMode.HALF_UP); // 소수점 2자리 반올림
        } else {
            this.usAvgAccuracyRate = BigDecimal.ZERO;
        }
    }
}
