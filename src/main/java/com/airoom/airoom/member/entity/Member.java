package com.airoom.airoom.member.entity;

import jakarta.persistence.Entity;

@Entity
public class Member {
    private Long memerNo;
    private String memberId;
    private String memberPw;
    private String memberName;
    private Integer memberAge;
    private String memberSchool;
    private String memberEmail;
    private Gender memberGender;
    private Grade memberGrade;
    private String memberClass;
}
