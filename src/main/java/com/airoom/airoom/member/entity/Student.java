package com.airoom.airoom.member.entity;

import com.airoom.airoom.member.model.dto.StudentDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Getter
// JPA는 기본 생성자로 생성 | 개발자의 무분별한 생성을 막기 위해
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
// Soft Delete 방식
@SQLDelete(sql = "UPDATE Student SET deleted_at = NOW() WHERE member_no = ?")
@SQLRestriction("deleted_at IS NULL")
// Hibernate 6에서 추가된 모든 SELECT가 실행이 될때 자동으로 WHERE 조건 추가

@Table(name = "STUDENT")

public class Student extends Member{

    private Integer studentClassNo;

    public StudentDto convertToStudent(){
        return StudentDto.builder()
                .memberId(super.getMemberId())
                .memberPw(super.getMemberPw())
                .memberName(super.getMemberName())
                .memberAge(super.getMemberAge())
                .memberSchool(super.getMemberSchool())
                .memberEmail(super.getMemberEmail())
                .memberGender(super.getMemberGender())
                .memberGrade(super.getMemberGrade())
                .memberClass(super.getMemberClass())
                .memberClassNo(studentClassNo)
                .build();
    }


}
