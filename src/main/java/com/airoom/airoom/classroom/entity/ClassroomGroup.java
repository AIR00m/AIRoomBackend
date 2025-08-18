package com.airoom.airoom.classroom.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
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
@SQLDelete(sql = "UPDATE classroom_group SET deleted_at = NOW() WHERE group_no = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClassroomGroup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupNo; // 그룹 고유번호

    private String groupName; // 그룹 이름

    // 클래스룸 학생의 고유번호를 참조 하지 않고 단독으로
    private Integer groupLeaderNo; // 그룹장 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_no")
    private Classroom classroom; // 클래스룸 고유번호
}
