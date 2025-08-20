package com.airoom.airoom.exam.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE created_exam_problem SET deleted_at = NOW() WHERE cep_no = ?")
@AllArgsConstructor
public class CreatedExamProblem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cepNo; //시험출제문제 고유번호

    @Column(nullable = false)
    private Integer cepQuestionOrder;  //시험출제문제 문항순서

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_no")
    @Setter
    private Exam exam; //시험

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ep_no")
    private ExamProblem examProblem; //시험 문제
}
