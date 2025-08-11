package com.airoom.airoom.member.controller;

import com.airoom.airoom.member.model.dto.StudentDto;
import com.airoom.airoom.member.model.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<?> login(@RequestBody StudentDto dto) {

    }




}
