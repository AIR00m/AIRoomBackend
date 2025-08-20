package com.airoom.airoom.board.entity;

import com.airoom.airoom.board.BoardType;
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
@SQLDelete(sql = "UPDATE attachment SET deleted_at = NOW() WHERE attach_no = ?")
@SQLRestriction("deleted_at IS NULL")
public class Attachment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachNo;
    // 첨부 파일 고유 번호

    @Column(nullable = false)
    private Long boardNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BoardType boardType;// 게시글 정보

    @Column(nullable = false)
    private String originalName;
    // 첨부 파일 원본 이름

    @Column(nullable = false)
    private String savedName;
    //첨부파일 저장 이름 -> 파일관리용

    @Column(nullable = false)
    private String s3Key;
    //presignedUrl 생성용
}
