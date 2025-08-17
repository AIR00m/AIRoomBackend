package com.airoom.airoom.member.model.repository;

import com.airoom.airoom.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,Integer> {
}
