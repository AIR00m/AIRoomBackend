package com.airoom.airoom.statistic.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_ls_pk", columnList = "ls_classroom_student_no, ls_type, ls_start_date, deleted_at"),
                @Index(name = "idx_ls_pk_end_date", columnList = "ls_classroom_student_no, ls_type, ls_start_date, ls_end_date, deleted_at"),
                @Index(name = "idx_ls_pk_exclude_start_date", columnList = "ls_classroom_student_no, ls_type, ls_end_date, deleted_at"),
                @Index(name = "idx_ls_student_type_created", columnList = "ls_classroom_student_no, ls_type, created_at DESC, deleted_at")
        }
)
/**
 * 학습 통계 테이블
 * 일별, 주별, 월별
 * 스케줄러를 통해 데이터 배치 처리
 * 매일 전날 데이터 추가
 * 매주 전주 데이터 추가
 * 매월 전월 데이터 추가
 */
public class LearningSummary extends BaseEntity {
    @EmbeddedId
    private LearningSummaryId id; //복합키

    private LocalDate lsEndDate; //통계 종료일

    private Integer lsTotalLearningDays; //학습일 수

    private Duration lsTotalLearningTime; //총 학습시간(밀리초)

    @Formula("ls_total_learning_time")
    private Long lsTotalLearningTimeMs; //집계용 컬럼

    private Integer lsTotalProblemsSolved; //문제풀이 수

    private Integer lsTotalCorrectProblems; //정답 수

    @Column(precision = 5, scale = 2)
    private BigDecimal lsAccuracyRate; //정답률

    private Integer lsAnomalyTotalCount; //이상현상 횟수
}
