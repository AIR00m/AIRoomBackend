package com.airoom.airoom.exam.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.exam.entity.value.ProblemLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("DELETED_AT IS NULL")
@SQLDelete(sql = "UPDATE EXAM_PROBLEM SET DELETED_AT = NOW() WHERE EP_NO = ?")
public class ExamProblem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long epNo;

    @Column(nullable = false)
    private String epQuestion; //문제 문항

    @Column(nullable = false)
    private Integer epQuestionNo; //문제 문항번호

    private String epImage; //문제 사진

    @Column(nullable = false)
    private String epParagraph; //문제 지문

    @Column(nullable = false)
    private String epExample; //문제 보기

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemLevel epLevel; //문제 난이도

    @Column(nullable = false)
    private String epAnswer; //문제 정답

    @Column(nullable = false)
    private String epComment; //문제 해설

    @Column(nullable = false)
    private Integer epUnit; //문제 단원
}
