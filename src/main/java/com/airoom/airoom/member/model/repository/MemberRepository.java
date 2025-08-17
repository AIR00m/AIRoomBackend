package com.airoom.airoom.member.model.repository;

import com.airoom.airoom.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Integer> {
    Optional<Member> findByMemberId(String memberId);
    boolean existsByMemberId(String memberId);
    boolean existsByMemberEmail(String memberEmail);
}
