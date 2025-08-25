package com.airoom.airoom.statistic.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_classroom_student_no", columnList = "bs_classroom_student_no")
})
/**
 * 행동 통계 테이블
 */
public class BehaviorSummary extends BaseEntity {
    @Id
    private Long bsClassroomStudentNo; //클래스룸 학생 고유번호

    private Duration bsTotalLearningTimeSec; //누적 개념 학습시간(초)

    private Duration bsTotalSolvedTimeSec; //누적 문제 풀이시간(초)

    @Column(columnDefinition = "json")
    private String bsHourlyDistribution; // 시간대별 누적 학습시간 ex) {"00": 123, ..., "23": 456}

    @Column(columnDefinition = "json")
    private String bsWeekdayDistribution; // 요일별 누적 학습시간 ex) {"Mon": 1234, ..., "Sun": 456}
}
