package com.airoom.airoom.board.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
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
@SQLDelete(sql = "UPDATE Homework SET deleted_at = NOW() WHERE board_no = ?")
@SQLRestriction("deleted_at IS NULL")

public class Homework extends Board {

    private boolean homeworkSubmitType = false;
    // 과제 제출 여부

    private Integer homeworkScore;
    // 과제 점수

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ASSIGN_TARGET_NO", nullable = false)
    private AssignTarget assignTarget;
    // 과제 대상

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ATTACH_NO", unique = true)
    private Attachment attachment;
    // 한 개의 homework 당 파일 1개를 위해서 unique
    // 단일 파일)
}
