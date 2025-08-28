package com.airoom.airoom.member.model.service;

import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.repository.MemberRepository;
import com.airoom.airoom.textbook.entity.Textbook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;

    public Long findMemberNoByMemberId(String memberId) {
        try {
            return memberRepository.findByMemberId(memberId)
                    .map(Member::getMemberNo)
                    .orElse(null);
        } catch (Exception e) {
            log.error("memberId로 memberNo 조회 중 오류: {}", memberId, e);
            return null;
        }
    }

}


