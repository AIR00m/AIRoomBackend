package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {
}
