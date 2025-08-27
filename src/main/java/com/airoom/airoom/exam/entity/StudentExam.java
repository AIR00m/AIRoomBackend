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
@SQLDelete(sql = "UPDATE student_exam SET deleted_at = NOW() WHERE se_no = ?")
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_exam_no_classroom_student_no", columnList = "exam_no, classroom_student_no, deleted_at"),
        @Index(name = "idx_exam_no", columnList = "exam_no, deleted_at"),
        @Index(name = "idx_exam_no_se_is_done", columnList = "exam_no, se_is_done, deleted_at"),
})
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
    @JoinColumn(name = "exam_no")
    @Setter
    private Exam exam; //시험

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_student_no")
    private ClassroomStudent classroomStudent; //클래스룸 학생

    @PrePersist
    public void prePersist() {
        if (seIsDone == null) {
            seIsDone = false;
        }
    }

    public void updateStudentExam(int seScore, LocalDateTime seStartTime, LocalDateTime seEndTime) {
        this.seIsDone = true;
        this.seScore = seScore;
        this.seStartTime = seStartTime;
        this.seEndTime = seEndTime;
    }
}
