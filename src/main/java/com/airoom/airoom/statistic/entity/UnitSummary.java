package com.airoom.airoom.statistic.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_us_student_unit_type_date", columnList = "us_classroom_student_no, us_unit_no, us_type, us_start_date, us_end_date, deleted_at")
        }
)
/**
 * 단원별 학습 통계 테이블
 * 일별/주별/월별 단위로 학생의 단원별 학습현황을 저장
 */
public class UnitSummary extends BaseEntity {
    @EmbeddedId
    private UnitSummaryId id; // 복합키: (학생, 단원, 타입, 시작일)

    private LocalDate usEndDate; // 통계 종료일

    private Integer usTotalLearningDays; // 학습일 수

    private Long usTotalLearningTimeMs; // 총 학습시간 (밀리초)

    private Integer usTotalProblemsSolved; // 총 문제 풀이 수

    private Integer usTotalCorrectProblems; // 정답 수

    @Column(precision = 5, scale = 2)
    private BigDecimal usAccuracyRate; // 정답률
}
