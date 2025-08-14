package com.airoom.airoom.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    private LocalDateTime assignStart;

    private LocalDateTime assignEnd;

    @Column(name = "ASSIGN_TITLE")
    private String assignBoardTitle;
}
