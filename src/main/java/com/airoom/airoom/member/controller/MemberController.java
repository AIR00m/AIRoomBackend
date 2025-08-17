package com.airoom.airoom.member.controller;

import com.airoom.airoom.member.model.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class MemberController {

    private final MemberService memberService;

    // 학생 회원가입
    @PostMapping("/signup/student")
    public ResponseEntity<?> enrollStudent(@RequestBody  StudentDto dto) {

    }

    // 선생님 회원가입
    @PostMapping("/signup/teacher")
    public ResponseEntity<?> enrollStudent(@RequestBody TeacherDto dto){

    }

    // 로그인





}
