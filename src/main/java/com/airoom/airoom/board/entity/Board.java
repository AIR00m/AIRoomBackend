package com.airoom.airoom.board.entity;

import com.airoom.airoom.board.BoardType;
import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 게시판의 공통 컬럼(제목은 포함되지 않음) -> 과제 제출(Homework)도 상속을 받아서 사용하기 때문

public abstract class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardNo;
    // 게시판 고유 번호(게시판들의 공통 PK)

    private String boardContent;
    // 게시판 내용

    @Enumerated(EnumType.STRING)
    private BoardType boardType;
    // GROUP(모둠),ASSIGN(과제),SUBJECT(과목),HOMEWORK(과제 제출) 게시판이 존재한다.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO")
    private Member member;
    // 회원 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    // Optional = false 이 관계는 null이 될 수 없다.
    @JoinColumn(name = "CLASSROOM_NO")
    private Classroom classroom;
    // 클래스룸 고유번호

}
