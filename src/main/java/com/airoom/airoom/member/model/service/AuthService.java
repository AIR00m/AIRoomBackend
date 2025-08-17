package com.airoom.airoom.member.model.service;

import com.airoom.airoom.common.value.MemberRole;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.dto.SignUpRequest;
import com.airoom.airoom.member.model.repository.MemberRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.DuplicateResourceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

//    public ResponseEntity<?> studentSignUp(SignUpRequest signUpRequest){
//    Service 에서는 dto와 domain만 리턴하고 Http 응답 포멧은 Controller 에서 -> 계층분리 유지보수 굿

    public Member studentSignUp(@Valid SignUpRequest signUpRequest){
        return signUp(signUpRequest , MemberRole.STUDENT);
    }

    public Member teacherSignUp(@Valid SignUpRequest signUpRequest){
        return signUp(signUpRequest , MemberRole.TEACHER);
    }

    private Member signUp(SignUpRequest signUpRequest, MemberRole memberRole) {
        // 아이디 중복 체크
        if(memberRepository.existsByMemberId(signUpRequest.id()))
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        // 서비스 코드 차원에서 사전에 중복을 체크해서 던진 예외
        // existBy -> save 사이에 동시에 다른 요청이 들어오면 경쟁 조건 때문에 중복 가입 가능
        // 쉽게 말하면 이 로직이 돌때 다른 요청이 들어와서 실행이 될 수 있는 상황 (경쟁 조건)

        Member member = Member.builder()
                .memberId(signUpRequest.id())
                .memberPwd(passwordEncoder.encode(signUpRequest.pwd()))
                .memberName(signUpRequest.memberName())
                .memberAge(signUpRequest.age())
                .memberSchool(signUpRequest.school())
                .memberEmail(signUpRequest.email())
                .memberGender(signUpRequest.gender())
                .memberGrade(signUpRequest.grade())
                .memberClass(signUpRequest.classNo())
                .memberImage(signUpRequest.image())
                .memberType(memberRole)
                .build();
        try{
            memberRepository.save(member);
            return member;
        } catch(DataIntegrityViolationException e){
            throw new DuplicateResourceException("이미 사용중인 아이디입니다.");
            // 여기서 왜 한번 더 중복 체크를 하는 거?
            // DB Unique 제약조건 위반 시 진짜로 발생하는 예외
        }
    }


}
