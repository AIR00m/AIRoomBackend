package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.exam.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
}
