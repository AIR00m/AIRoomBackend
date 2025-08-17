package com.airoom.airoom.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE EXAM SET DELETED_AT = NOW() WHERE EXAM_NO = ?")
@AllArgsConstructor
public class CreatedExamProblem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cepNo; //시험출제문제 고유번호

    @Column(nullable = false)
    private Integer cepQuestionOrder;  //시험출제문제 문항순서

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXAM_NO")
    private Exam exam; //시험

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EP_NO")
    private ExamProblem examProblem; //시험 문제
}
