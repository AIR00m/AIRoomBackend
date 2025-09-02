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
 * 우리반 학습현황 DTO
 */
public class ClassroomLearningSummaryAllResponse {
    private Long studentNo; //학생 고유번호(필수)
    private String studentName; //학생이름(필수)
    private BigDecimal studentLearningProgress; //학생 수업진도 퍼센트(필수)
    private Long studentTotalLearningTime; //학생 총 학습시간(밀리초) => 수업 + 시험(필수)
    private Long studentTotalProblemSolved; //학생 총 푼 문제수
    private Long studentTotalCorrectProblems; //학생 총 맞은 문제수
    private Long studentTotalProgressPages = 0L; //학생 진도 페이지 수 총합
    private Long textbookTotalPages; //교과서 페이지 수 총합
    private Long studentTotalAssignScore; //학생 과제점수 총합
    private Long studentTotalSubmitAssign; //학생 과제제출 수 총합
    private BigDecimal studentAvgExamScore; //학생 평균시험 점수(필수)
    private BigDecimal studentAvgAssignScore; //학생 평균과제 점수(필수)
    private Long studentAnomalyTotalCount; //학생 이상현황 총 횟수(필수)

    public ClassroomLearningSummaryAllResponse(Long studentNo, String studentName, Long studentTotalLearningTime, Long studentTotalProblemSolved, Long studentTotalCorrectProblems, Long studentTotalProgressPages, Long studentTotalAssignScore, Long studentTotalSubmitAssign, Long studentAnomalyTotalCount) {
        this.studentNo = studentNo;
        this.studentName = studentName;
        this.studentTotalLearningTime = studentTotalLearningTime;
        this.studentTotalProblemSolved = studentTotalProblemSolved;
        this.studentTotalCorrectProblems = studentTotalCorrectProblems;
        this.studentTotalProgressPages = studentTotalProgressPages;
        this.studentTotalAssignScore = studentTotalAssignScore;
        this.studentTotalSubmitAssign = studentTotalSubmitAssign;
        this.studentAnomalyTotalCount = studentAnomalyTotalCount;
    }

    public void calcAvgAll() {
        if (this.studentTotalProblemSolved > 0) {
            this.studentAvgExamScore = BigDecimal.valueOf(this.studentTotalCorrectProblems * 100.0 / this.studentTotalProblemSolved)
                    .setScale(2, RoundingMode.HALF_UP); // 소수점 2자리 반올림
        } else {
            this.studentAvgExamScore = BigDecimal.ZERO;
        }

        if (this.textbookTotalPages > 0) {
            this.studentLearningProgress = BigDecimal.valueOf(this.studentTotalProgressPages * 100.0 / this.textbookTotalPages).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.studentLearningProgress = BigDecimal.ZERO;
        }

        if (studentTotalSubmitAssign > 0) {
            this.studentAvgAssignScore = BigDecimal.valueOf(this.studentTotalAssignScore / this.studentTotalSubmitAssign)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.studentAvgAssignScore = BigDecimal.ZERO;
        }
    }


}
