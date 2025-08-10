package com.airoom.airoom.member.model.repository;

import com.airoom.airoom.member.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,Long> {
    boolean existsByMemberId(String memberId);
    Optional<Student> findByMemberId(String memberId);
}
