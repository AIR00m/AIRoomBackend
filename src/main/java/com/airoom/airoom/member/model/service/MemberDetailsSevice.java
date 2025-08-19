package com.airoom.airoom.member.model.service;

import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberDetailsSevice implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findMemberByMemberId(username)
                .orElseThrow(() -> new UsernameNotFoundException("저장된 회원이 존재하지 않습니다."));

        String role;


        return null;
    }
}
