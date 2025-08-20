package com.airoom.airoom.member.controller;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.classroom.model.service.ClassroomService;
import com.airoom.airoom.common.token.CookieUtility;
import com.airoom.airoom.common.token.JWTTokenUtility;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.dto.LoginRequest;
import com.airoom.airoom.member.model.dto.SignUpRequest;
import com.airoom.airoom.member.model.service.AuthService;
import com.airoom.airoom.member.model.service.MemberService;
import com.airoom.airoom.textbook.entity.Textbook;
import com.airoom.airoom.textbook.model.service.TextbookService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TextbookService textbookService;

    //학생 회원가입
    @PostMapping("/signup/student")
    public ResponseEntity<?> enrollStudent(@RequestBody SignUpRequest request) {
        Member member;
        return null;
    }

    // 선생님 회원가입
    @PostMapping("/signup/teacher")
    public ResponseEntity<?> enrollTeacher(@RequestBody SignUpRequest dto) {
        return null;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        Member member = authService.authenticate(request.id(), request.pwd());
        String role = member.getMemberType().toString();
        List<Textbook> textbooks = new ArrayList<>();

        if (role.equals("TEACHER")) {
            textbooks = textbookService.getAllTextbooksByTeacherMemberNo(member.getMemberNo());
        } else {
            textbooks = textbookService.getAllTextbooksByStudentMemberNo(member.getMemberNo());
        }
        return ResponseEntity.ok()
                .body(Map.of("memberId", member.getMemberId(), "textbooks", textbooks));

    }


}