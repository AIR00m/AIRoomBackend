package com.airoom.airoom.member.controller;

import com.airoom.airoom.classroom.model.service.ClassroomService;
import com.airoom.airoom.common.token.CookieUtility;
import com.airoom.airoom.common.token.JWTTokenUtility;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.dto.LoginRequest;
import com.airoom.airoom.member.model.dto.SignUpRequest;
import com.airoom.airoom.member.model.dto.SignUpResponse;
import com.airoom.airoom.member.model.dto.TokenRequest;
import com.airoom.airoom.member.model.service.AuthService;
import com.airoom.airoom.textbook.entity.Textbook;
import com.airoom.airoom.textbook.model.service.TextbookService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements AuthControllerSwagger {

    private final AuthService authService;
    private final TextbookService textbookService;
    private final ClassroomService classroomService;
    private final JWTTokenUtility jwtUtility;
    private final CookieUtility cookieUtility;


    /**
     * 회원가입
     */
    @PostMapping("/signup/student")
    public ResponseEntity<SignUpResponse> enrollStudent(@Valid @RequestBody SignUpRequest request) {
        Member member = authService.studentSignUp(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignUpResponse(member.getMemberName()));
    }

    // 선생님 회원가입
    @PostMapping("/signup/teacher")
    public ResponseEntity<SignUpResponse> enrollTeacher(@Valid @RequestBody SignUpRequest request) {
        Member member = authService.teacherSignUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignUpResponse(member.getMemberName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Member member = authService.authenticate(request.id(), request.pwd());
        String role = member.getMemberType().toString();
        List<Textbook> textbooks;

        if (role.equals("TEACHER")) {
            textbooks = textbookService.getAllTextbooksByTeacherMemberNo(member.getMemberNo());
        } else {
            textbooks = textbookService.getAllTextbooksByStudentMemberNo(member.getMemberNo());
        }
        return ResponseEntity.ok()
                .body(Map.of("memberId", member.getMemberId(), "textbooks", textbooks));

    }

    @PostMapping("/token")
    public ResponseEntity<?> createTokenOnTextbookClick(@Valid @RequestBody TokenRequest tokenRequest) {
        // 프론트에서 아이디를 넘겨준것으로 맴버를 반환
        Member member = authService.searchById(tokenRequest.memberId());
        // 역할 확인
        boolean isTeacher = member.getMemberType().toString().equals("TEACHER");

        // 추가로 넣어줄 클래임들 생성
        Map<String, Object> classroomClaims = new HashMap<>();
        classroomClaims.put("memberName", member.getMemberName());

        if (isTeacher) {
            Long classroomNo = classroomService.getClassroomNoByTeacherId(tokenRequest.memberId(), tokenRequest.textbookNo());
            classroomClaims.put("classroomNo", classroomNo);
            Long classroomTeacherNo = classroomService.getClassTeacherNoByClassRoomNo(classroomNo);
            classroomClaims.put("classroomTeacherNo", classroomTeacherNo);
            classroomClaims.put("memberNo", member.getMemberNo());
        } else {
            Long classroomNo = classroomService.getClassroomNoByStudentId(tokenRequest.memberId(), tokenRequest.textbookNo());
            classroomClaims.put("classroomNo", classroomNo);
            Long classRoomStudentNo = classroomService.getClassStudentNoByClassRoomNoAndId(classroomNo, member.getMemberId());
            classroomClaims.put("classRoomStudentNo", classRoomStudentNo);
            classroomClaims.put("memberNo", member.getMemberNo());
        }

        // 3. 서버에서 토큰을 발급
        String accessToken = jwtUtility.createAccessToken(member, isTeacher, classroomClaims);
        String refreshToken = jwtUtility.createRefreshToken(tokenRequest.memberId(), isTeacher);


        // 레디스 블랙리스트 만들기!

        ResponseCookie cookie = cookieUtility.refreshTokenCookie(refreshToken);
        // 쿠키에 담는 것도 좋지만 프론트에서 localstorage에 담는것도 생각해보는 것을 추천

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("Access_Token", accessToken));

    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseCookie> deleteToken() {
        return ResponseEntity.ok(cookieUtility.deleteTokenCookie());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "Refresh_Token", required = false) String refreshToken) {

        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Refresh_Token이 존재하지 않습니다."));
            }


            Claims claims = JWTTokenUtility.verifyRefreshToken(refreshToken);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);


            Member member = authService.searchById(userId);
            boolean isTeacher = "teacher".equals(role);

            // 기본적인 클래임만 왜냐하면 localStorage 안에 영구히 저장 되는 형태이기 때문에 그걸 사용한다.
            Map<String, Object> basicClaims = new HashMap<>();
            basicClaims.put("memberName", member.getMemberName());
            basicClaims.put("memberNo", member.getMemberNo());


            String newAccessToken = jwtUtility.createAccessToken(member, isTeacher, basicClaims);


//            새로 발급하는 것이 보안에 더욱 적절함 탈취 시 1회만 사용 가능
//            문제점 : 동시 요청시 문제 / 서버 부하

//            String newRefreshToken = jwtUtility.createRefreshToken(userId, isTeacher);
//            ResponseCookie newCookie = cookieUtility.refreshTokenCookie(newRefreshToken);

            return ResponseEntity.ok()
                    .body(Map.of("Access_Token", newAccessToken));

        } catch (JwtException e) {
            ResponseCookie deleteCookie = cookieUtility.deleteTokenCookie();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body(Map.of("message", "Refresh Token이 만료되었습니다."));
        }
    }
}

