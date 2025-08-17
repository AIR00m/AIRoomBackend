package com.airoom.airoom.exam.entity;

import com.airoom.airoom.textbook.entity.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE EXAM_UNIT SET DELETED_AT = NOW() WHERE EU_NO = ?")
@AllArgsConstructor
public class ExamUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long euNo; //시험단원 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXAM_NO")
    private Exam exam; //시험

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_NO")
    private Unit unit; //단원
}
