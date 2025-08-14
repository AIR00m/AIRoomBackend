package com.airoom.airoom.classroom.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.common.value.Grade;
import com.airoom.airoom.common.value.Semester;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
// JPA는 기본 생성자로 생성 | 개발자의 무분별한 생성을 막기 위해
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
// Soft Delete 방식
@SQLDelete(sql = "UPDATE CLASSROOM SET deleted_at = NOW() WHERE CLASSROOM_NO = ?")
@SQLRestriction("deleted_at IS NULL")
public class Classroom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classroomNo;
    @Column(nullable = false)
    private String classroomSchool;
    @Column(nullable = false)
    private Grade classroomGrade;
    @Column(nullable = false)
    private String classroomClass;
    @Column(nullable = false)
    private Integer classroomYear;
    @Column(nullable = false)
    private Semester classroomSemester;
}
