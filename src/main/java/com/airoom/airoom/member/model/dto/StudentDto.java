package com.airoom.airoom.member.model.dto;

import com.airoom.airoom.member.entity.Gender;
import com.airoom.airoom.member.entity.Grade;
import com.airoom.airoom.member.entity.Student;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class StudentDto {

    @NotBlank(message = "ID는 필수 값입니다.")
    @Column(unique = true, nullable = false, length = 8)
    private String memberId;
    @NotBlank(message = "비밀번호는 필수 값입니다.")
    private String memberPw;
    @NotBlank(message = "이름은 필수 값입니다.")
    private String memberName;
    @NotBlank
    private Integer memberAge;
    @NotBlank
    private String memberSchool;
    @NotBlank
    private String memberEmail;
    @NotBlank
    private Gender memberGender;
    @NotBlank
    private Grade memberGrade;
    @NotBlank
    private Integer memberClass;
    @NotBlank
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
