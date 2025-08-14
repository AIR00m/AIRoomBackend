package com.airoom.airoom.textbook.entity;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE PROGRESS SET DELETED_AT = NOW() WHERE PROGRESS_NO = ?")
@AllArgsConstructor
/**
 * 진도 엔티티
 */
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressNo; //진도 고유번호

    @Column(nullable = false)
    private Integer progressLastPage; //진도 마지막페이지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_NO")
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASSROOM_STUDENT_NO")
    private ClassroomStudent classroomStudent;
}
