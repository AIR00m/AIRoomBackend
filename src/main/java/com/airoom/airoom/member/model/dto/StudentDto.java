package com.airoom.airoom.member.model.dto;

import com.airoom.airoom.member.entity.Gender;
import com.airoom.airoom.common.value.Grade;
import com.airoom.airoom.member.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class StudentDto {

    private String memberId;
    private String memberPw;
    private String memberName;
    private Integer memberAge;
    private String memberSchool;
    private String memberEmail;
    private Gender memberGender;
    private Grade memberGrade;
    private Integer memberClass;
    private Integer memberClassNo;

    public Student convertToStudent() {
        return Student.builder()
                .memberId(memberId)
                .memberPw(memberPw)
                .memberName(memberName)
                .memberAge(memberAge)
                .memberSchool(memberSchool)
                .memberEmail(memberEmail)
                .memberGender(memberGender)
                .memberGrade(memberGrade)
                .memberClass(memberClass)
                .studentClassNo(memberClassNo)
                .build();
    }
}
