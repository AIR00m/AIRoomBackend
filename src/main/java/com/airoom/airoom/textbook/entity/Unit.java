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
    private Long unitNo;

    @Column(nullable = false)
    private String unitTitle;

    @Column(nullable = false)
    private Integer unitNum;

    @Column(nullable = false)
    private Integer unitStart;

    @Column(nullable = false)
    private Integer unitEnd;

    @Column(nullable = false)
    private String unitPdfUrl;

    @Column(nullable = false)
    private String unitImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEXTBOOK_NO", nullable = false)
    private Textbook textbook;
}
