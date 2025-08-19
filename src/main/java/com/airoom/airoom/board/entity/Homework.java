package com.airoom.airoom.board.entity;

import com.airoom.airoom.classroom.entity.Classroom;
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
@SQLDelete(sql = "UPDATE homework SET deleted_at = NOW() WHERE homework_board_no = ?")
@SQLRestriction("deleted_at IS NULL")

public class Homework extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long homeworkBoardNo;
    // 게시판 고유 번호(게시판들의 공통 PK)

    @Column(nullable = false)
    private String homeworkBoardContent;
    // 게시판 내용

    private boolean homeworkSubmitType = false;
    // 과제 제출 여부

    private Integer homeworkScore;
    // 과제 점수

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assign_target_no", nullable = false)
    private AssignTarget assignTarget;
    // 과제 대상

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no")
    private Member member;
    // 회원 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    // Optional = false 이 관계는 null이 될 수 없다.
    @JoinColumn(name = "classroom_no")
    private Classroom classroom;
    // 클래스룸 고유번호
}
