package com.airoom.airoom.member.model.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
        @NotBlank(message = "아이디는 필수입니다.") 
        String id,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String pwd
    ){}
