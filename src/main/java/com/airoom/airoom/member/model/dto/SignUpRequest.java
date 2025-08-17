package com.airoom.airoom.member.model.dto;

import com.airoom.airoom.common.value.Grade;

import com.airoom.airoom.common.value.MemberRole;
import com.airoom.airoom.member.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record SignUpRequest(
        // 생성 전용 DTO 이기 때문에 바뀌 것이 없으므로 -> Record(생성 시, 필드 변경이 불가)
        // 어노테이션을 해주지 않아도 어노테이션이 자동으로 들어가져  있는 형태

        // class로 만들면 장점 추가 메서드 추가 장점 -> Setter, Builder
        // 수정에서 같은 DTO를 사용하고 싶을 때, 불변이라서 새로 만들어서 사용을 해야한다.

    @NotBlank // blank의 경우는 문자열 경우에 사용하는 것
    String id,

    @NotBlank
     String pwd,

    @NotBlank
     String memberName,

    @NotNull
     Integer age,

    @NotBlank
     String school,

    @NotBlank
    @Email
     String email,

    @NotBlank
     Gender gender,

    @NotBlank
     Grade grade,

    @NotBlank
     Integer classNo,

//    @NotBlank
//    MemberRole role,
        // 프론트에서 인위적으로 역할을 넣을 수 있는 것을 방지 하기 위해 role은 서버에서 해준다.
        // 어떤 엔드 포인트를 탔는가(버튼)

    @NotBlank
     String image
){}


