package com.airoom.airoom.classroom.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.textbook.entity.Textbook;
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
@SQLDelete(sql = "UPDATE CLASSROOM_TEACHER SET deleted_at = NOW() WHERE CLASSROOM_TEACHER_NO = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClassroomTeacher extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classroomTeacherNo; // 클래스룸 선생님 고유번호

    @ManyToOne
    @JoinColumn(name = "CLASSROOM_NO")
    private Classroom classroom; // 클래스룸 고유 번호

    @ManyToOne
    @JoinColumn(name ="MEMBER_NO")
    private Member teacher;// 회원 고유 번호 (선생님)

    @ManyToOne
    @JoinColumn(name = "TEXTBOOK_NO")
    private Textbook textbook; // 교재 고유 번호
}
