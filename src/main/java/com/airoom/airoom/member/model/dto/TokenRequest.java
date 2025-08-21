package com.airoom.airoom.member.model.dto;

public record TokenRequest (
    String memberId,
    Long textbookNo
){}
