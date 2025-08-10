package com.airoom.airoom.member.model.repository;

import com.airoom.airoom.member.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher,Long> {
    boolean existsByMemberId(String memberId);
    Optional<Teacher> findByMemberId(String memberId);
}
