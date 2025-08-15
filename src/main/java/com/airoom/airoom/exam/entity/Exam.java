package com.airoom.airoom.exam.entity;

import com.airoom.airoom.classroom.entity.ClassroomTeacher;
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
@SQLDelete(sql = "UPDATE EXAM SET DELETED_AT = NOW() WHERE EXAM_NO = ?")
@AllArgsConstructor
public class Exam extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examNo; //시험 고유번호

    @Column(nullable = false)
    private Integer examProblemCount; //시험 문제수

    private LocalDateTime examStartTime; //시험 시작시간

    private LocalDateTime examEndTime; //시험 종료시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASSROOM_TEACHER_NO")
    private ClassroomTeacher classroomTeacher; //클래스룸교사 고유번호
}
