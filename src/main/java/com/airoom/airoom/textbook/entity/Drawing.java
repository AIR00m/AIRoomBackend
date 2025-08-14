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
@SQLDelete(sql = "UPDATE DRAWING SET DELETED_AT = NOW() WHERE DRAWING_NO = ?")
@AllArgsConstructor
/**
 * 그림판 엔티티
 */
public class Drawing extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drawingNo; //그림판 고유번호

    private String drawingData; //그림정보

    private String drawingPage; //단원 페이지번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_NO", nullable = false)
    private Unit unit; //단원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASSROOM_STUDENT_NO")
    private ClassroomStudent classroomStudent; //클래스룸 학생
}