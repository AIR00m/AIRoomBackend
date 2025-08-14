package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE UNIT SET DELETED_AT = NOW() WHERE UNIT_NO = ?")
@AllArgsConstructor
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unitNo; //단원 고유번호

    @Column(nullable = false)
    private String unitTitle; //단원명

    @Column(nullable = false)
    private Integer unitNum; //단원번호

    @Column(nullable = false)
    private Integer unitPages; //단원 총페이지 수

    @Column(nullable = false)
    private String unitPdfUrl; //단원PDF URL

    @Column(nullable = false)
    private String unitImageUrl; //단원이미지 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEXTBOOK_NO", nullable = false)
    private Textbook textbook; //교재
}
