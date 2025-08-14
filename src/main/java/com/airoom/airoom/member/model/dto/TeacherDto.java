package com.airoom.airoom.member.model.dto;

import com.airoom.airoom.member.entity.Gender;
import com.airoom.airoom.common.value.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TeacherDto {

    private String memberId;
    private String memberPw;
    private String memberName;
    private Integer memberAge;
    private String memberSchool;
    private String memberEmail;
    private Gender memberGender;
    private Grade memberGrade;
    private Integer memberClass;


}

