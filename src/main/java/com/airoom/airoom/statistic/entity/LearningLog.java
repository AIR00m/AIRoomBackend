package com.airoom.airoom.statistic.entity;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.exam.entity.CreatedExamProblem;
import com.airoom.airoom.statistic.entity.value.LogType;
import com.airoom.airoom.textbook.entity.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_classroom_student", columnList = "classroom_student_no"), //학생별 로그조회용
        @Index(name = "idx_classroom_student_start_time", columnList = "classroom_student_no, ll_start_time"), //기간별 학생로그 조회용
        @Index(name = "idx_unit", columnList = "unit_no"), //단원별 조회용
        @Index(name = "idx_cep", columnList = "cep_no"), //시험문제별 조회용
        @Index(name = "idx_log_type", columnList = "ll_type") //로그 타입별 조회용
})
/**
 * 원천 학습로그 테이블
 * 프론트 로그수집 -> Kafka API로 전송 (일정 주기/트리거)에 의해서 -> logstash 전송 -> ElasticSearch에 저장 -> Kibana 시각화 && LEARNING_LOG(RDB)에 저장
 * 이후 요약 통계 테이블(LEARNING_SUMMARY, LEARNING_BEHAVIOR)로 배치 처리할 것!
 */
public class LearningLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long llNo; //학습로그 고유번호

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private LogType llType; //학습로그 유형(LEARN, EXAM)

    @Column(nullable = false)
    private LocalDateTime llStartTime; //학습시작 시간

    @Column(nullable = false)
    private LocalDateTime llEndTime; //학습종료 시간

    @Column(nullable = false)
    private Duration llDurationSec; //학습시간

    private String selectedAnswer; //선택한 답

    @Builder.Default
    //정답여부 (시험의 경우에만 필요하며 바로 값을 넣어주는게 아닌 RDB insert 시점에 선택한 답과 ExamProblem의 답을 비교해서 정답여부 지정)
    private Boolean llIsCorrect = false; 

    @PrePersist
    public void prePersist() {
        if (llIsCorrect == null) {
            llIsCorrect = false;
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_student_no")
    private ClassroomStudent classroomStudent; //클래스룸 학생

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_no")
    private Unit unit; //단원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cep_no")
    private CreatedExamProblem createdExamProblem; //시험출제문제
}
