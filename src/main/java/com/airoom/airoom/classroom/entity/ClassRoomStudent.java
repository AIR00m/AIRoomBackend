package com.airoom.airoom.classroom.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.member.entity.Member;
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
@SQLDelete(sql = "UPDATE Student SET deleted_at = NOW() WHERE member_no = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClassRoomStudent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classRoomStudentNo;
    @ManyToOne
    @JoinColumn(name = "CLASSROOM_NO")
    private ClassRoom classRoom;
    @ManyToOne
    @JoinColumn(name ="MEMBER_NO")
    private Member student;
    @ManyToOne
    @JoinColumn(name = "GROUP_NO")
    private Group group;
}
