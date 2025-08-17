package com.airoom.airoom.exam.entity;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE STUDENT_EXAM SET DELETED_AT = NOW() WHERE SE_NO = ?")
@AllArgsConstructor
/**
 * 학생 시험 엔티티
 * 시험 엔티티 생성 시 트랜잭션으로 묶어서 같이 생성
 */
public class StudentExam extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seNo; //학생시험 고유번호

    @Builder.Default
    @Column(nullable = false)
    private Boolean seIsDone = false; //학생시험 응시여부

    private Integer seScore; //학생시험 점수

    private LocalDateTime seStartTime; //학생시험 시작시간

    private LocalDateTime seEndTime; //학생시험 종료시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXAM_NO")
    private Exam exam; //시험

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASSROOM_STUDENT_NO")
    private ClassroomStudent classroomStudent; //클래스룸 학생

    @PrePersist
    public void prePersist() {
        if (seIsDone == null) {
            seIsDone = false;
        }
    }
}
