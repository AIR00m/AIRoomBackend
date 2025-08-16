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
    // 과목 게시판 (예시 페이지에서 게시판 아이콘을 클릭했을 때 나오는 게시판)

    @Column(name = "SB_TITLE")
    private String SubjectBoardTitle;
    // 과목게시판 제목

    @Column(name = "SB_FOCUS_TYPE")
    private Boolean topFixed;
    // 상단 고정 여부
}
