package com.airoom.airoom.exam.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.exam.entity.value.ProblemLevel;
import com.airoom.airoom.textbook.entity.Unit;
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
    private Long epNo; //시험문제 고유번호

    @Column(nullable = false)
    private String epQuestion; //시험문제 문항

    private String epImageUrl; //시험문제이미지 URL

    private String epParagraph; //시험문제 지문

    private String epExample; //시험문제 보기

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemLevel epLevel; //시험문제 난이도

    @Column(nullable = false)
    private String epAnswer; //시험문제 정답

    private String epComment; //시험문제 해설

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_NO", nullable = false)
    private Unit unit; //시험문제 단원
}
