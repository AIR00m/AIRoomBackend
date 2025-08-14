package com.airoom.airoom.board.entity;

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
@SQLDelete(sql = "UPDATE SubjectBoard SET deleted_at = NOW() WHERE board_no = ?")
@SQLRestriction("deleted_at IS NULL")
public class SubjectBoard extends Board{

    @Column(name = "SB_TITLE")
    private String SubjectBoardTitle;

    @Column(name = "SB_FOCUS_TYPE")
    private Boolean topFixed;
}
