package com.airoom.airoom.member.model.repository;

import com.airoom.airoom.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findMemberByMemberId(String memberId);
    boolean existsByMemberId(String memberId);
    boolean existsByMemberEmail(String memberEmail);


}
