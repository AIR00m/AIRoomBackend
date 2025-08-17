package com.airoom.airoom.textbook.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.common.value.Grade;
import com.airoom.airoom.common.value.Semester;
import com.airoom.airoom.common.value.Subject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE TEXTBOOK SET DELETED_AT = NOW() WHERE TEXTBOOK_NO = ?")
@AllArgsConstructor
/**
 * 교재 엔티티
 */
public class Textbook extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long textbookNo; //교재 고유번호

    @Column(nullable = false)
    private String textbookTitle; //교재 제목

    @Column(nullable = false)
    private String textbookPublisher; //교재 출판사

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Subject textbookSubject = Subject.MATH; //교재 과목

    @Column(nullable = false)
    private String textbookPdfUrl; //교재 PDF URL

    @Column(nullable = false)
    private Grade textbookGrade; //교재 대상학년

    @Column(nullable = false)
    private Semester textbookSemester; //교재 대상학기

    @Column(nullable = false)
    private String textbookImageUrl; //교재 이미지 URL

    @PrePersist
    public void prePersist() {
        if (textbookSubject == null) {
            textbookSubject = Subject.MATH;
        }
    }
}
