package com.airoom.airoom.exam.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
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
@SQLDelete(sql = "UPDATE exam_unit SET deleted_at = NOW() WHERE eu_no = ?")
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_exam_no", columnList = "exam_no, deleted_at")
        }
)
public class ExamUnit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long euNo; //시험단원 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_no")
    @Setter
    private Exam exam; //시험

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_no")
    private Unit unit; //단원
}
