package com.bs.boot.myboardrest.project.config.member.controller;

import com.airoom.airoom.member.model.dto.StudentDto;
import com.airoom.airoom.member.model.dto.TeacherDto;
import com.airoom.airoom.member.model.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/student")
    public ResponseEntity<String> enrollStudent(@RequestBody StudentDto studentDto) {
        memberService.enrollStudent(studentDto);
        return ResponseEntity.ok("학생회원 가입 완료");
    }

    @PostMapping("/teacher")
    public ResponseEntity<String> enrollStudent(@RequestBody TeacherDto teacherDto) {
        memberService.enrollTeacher(teacherDto);
        return ResponseEntity.ok("학생회원 가입 완료");
    }



}
