package com.airoom.airoom.textbook.entity;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
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
@SQLDelete(sql = "UPDATE progress SET deleted_at = NOW() WHERE progress_no = ?")
@AllArgsConstructor
/**
 * 진도 엔티티
 */
public class Progress extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressNo; //진도 고유번호

    @Column(nullable = false)
    private Integer progressLastPage; //진도 마지막페이지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_no")
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_student_no")
    private ClassroomStudent classroomStudent;
}
