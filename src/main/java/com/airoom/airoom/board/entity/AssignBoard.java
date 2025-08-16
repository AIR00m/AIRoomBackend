package com.airoom.airoom.board.entity;

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
@SQLDelete(sql = "UPDATE AssignBoard SET deleted_at = NOW() WHERE board_no = ?")
@SQLRestriction("deleted_at IS NULL")
public class AssignBoard extends Board{
    // 과제 게시판
    @Column(nullable = false)
    private LocalDateTime assignStart;
    //  과제 제출 시작일
    @Column(nullable = false)
    private LocalDateTime assignEnd;
    // 과제 제출 마감일

    @Column(name = "ASSIGN_TITLE", nullable = false)
    private String assignBoardTitle;
    // 과제 게시판 제목

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ATTACH_NO")
    private Attachment attachment;
    // 단일 파일
}
