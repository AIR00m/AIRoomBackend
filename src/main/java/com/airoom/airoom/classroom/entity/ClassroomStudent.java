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
@SQLDelete(sql = "UPDATE CLASSROOM_STUDENT SET deleted_at = NOW() WHERE CLASSROOM_STUDENT_NO = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClassroomStudent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classRoomStudentNo; // 클래스룸 학생 고유 번호

    @ManyToOne
    @JoinColumn(name = "CLASSROOM_NO")
    private Classroom classRoom; // 클래스룸 고유 번호

    @ManyToOne
    @JoinColumn(name ="MEMBER_NO")
    private Member student; // 회원 고유번호(학생_

    @ManyToOne
    @JoinColumn(name = "GROUP_NO")
    private Group group; // 모둠 고유번호
}
