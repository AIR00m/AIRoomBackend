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

public abstract class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardNo;
    private String boardContent;
    @Enumerated(EnumType.STRING)
    private BoardType boardType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO")
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    // Optional = false 이 관계는 null이 될 수 없다.
    @JoinColumn(name = "CLASSROOM_NO")
    private Classroom classroom;
}
