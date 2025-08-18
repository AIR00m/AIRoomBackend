package com.airoom.airoom.board.entity;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
// JPA는 기본 생성자로 생성 | 개발자의 무분별한 생성을 막기 위해
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
// Soft Delete 방식
@SQLDelete(sql = "UPDATE assign_board SET deleted_at = NOW() WHERE assign_board_no = ?")
@SQLRestriction("deleted_at IS NULL")
public class AssignBoard extends BaseEntity {
    // 과제 게시판

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignBoardNo;
    // 게시판 고유 번호(게시판들의 공통 PK)

    @Column(nullable = false)
    private String assignBoardContent;
    // 게시판 내용

    @Column(nullable = false)
    private LocalDateTime assignStart;
    //  과제 제출 시작일

    @Column(nullable = false)
    private LocalDateTime assignEnd;
    // 과제 제출 마감일

    @Column(name = "assign_title", nullable = false)
    private String assignBoardTitle;
    // 과제 게시판 제목

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
