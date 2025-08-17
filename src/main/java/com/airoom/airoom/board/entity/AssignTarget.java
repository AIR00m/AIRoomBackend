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
@SQLDelete(sql = "UPDATE AssignTarget SET deleted_at = NOW() WHERE assign_target_no = ?")
@SQLRestriction("deleted_at IS NULL")


public class AssignTarget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignTargetNo;
    // 과제 대상 고유 번호

    @Column(nullable = false)
    private Long targetNo;
    // 모둠 고유번호 / 클래스룸 고유 번호를 단독 컬럼으로 사용

    @Column(nullable = false)
    private boolean groupAssignType;
    // 모둠과제여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="BOARD_NO")
    private AssignBoard assignBoard;
    // 과제 고유 번호

}
