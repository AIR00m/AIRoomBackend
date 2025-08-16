package com.airoom.airoom.member.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.common.value.Grade;
import com.airoom.airoom.common.value.MemberRole;
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
@SQLDelete(sql = "UPDATE MEMBER SET deleted_at = NOW() WHERE member_no = ?")
@SQLRestriction("deleted_at IS NULL")
// Hibernate 6에서 추가된 모든 SELECT가 실행이 될때 자동으로 WHERE 조건 추가

public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberNo; // 회원 고유 번호

    @Column(nullable = false)
    private String memberId; // 회원 아이디

    @Column(nullable = false)
    private String memberName; // 회원 패스 워드

    @Column(nullable = false)
    private Integer memberAge; // 회원 나이

    @Column(nullable = false)
    private String memberSchool; // 회원 소속 학교

    @Column(nullable = false)
    private String memberEmail; // 회원 이메일

    @Column(nullable = false)
    private Gender memberGender; // 회원 성별

    private Grade memberGrade; // 회원 학년

    private Integer memberClass; // 회원 반

    private String memberImage; //회원 프로필 사진

    @Column(nullable = false)
    private MemberRole memberType; // 회원 타입(선생님, 학생)

}
