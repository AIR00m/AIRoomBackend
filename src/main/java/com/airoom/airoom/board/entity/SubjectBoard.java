package com.airoom.airoom.board.entity;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE subject_board SET deleted_at = NOW() WHERE sb_no = ?")
@AllArgsConstructor
public class SubjectBoard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sbNo;

    @Column(nullable = false)
    private String sbTitle;

    @Column(nullable = false)
    private String sbContent;

    @Column(nullable = false)
    private boolean sbFocusType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO")
    private Member member;
// 회원 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
// Optional = false 이 관계는 null이 될 수 없다.
    @JoinColumn(name = "CLASSROOM_NO")
    private Classroom classroom;
// 클래스룸 고유번호

    public void updateSb(String title, String content, boolean focusType) {
        this.sbTitle = title;
        this.sbContent = content;
        this.sbFocusType = focusType;
    }
}
