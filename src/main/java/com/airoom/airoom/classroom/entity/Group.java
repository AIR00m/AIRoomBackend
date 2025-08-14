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
@SQLDelete(sql = "UPDATE GROUP SET deleted_at = NOW() WHERE groud_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Group extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    private String groupName;

    // 클래스룸 학생의 고유번호를 참조 하지 않고 단독으로 넣어주는 방식으로 사용
    private Integer groupLeaderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClASSROOM_NO")
    private Classroom classroom;
}
