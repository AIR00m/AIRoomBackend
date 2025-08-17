package com.airoom.airoom.member.model.dto;

import jakarta.validation.constraints.NotBlank;

public class SignUpRequest {
    @NotBlank
    private String memberId;
    @NotBlank
    private String memberPwd;

}
