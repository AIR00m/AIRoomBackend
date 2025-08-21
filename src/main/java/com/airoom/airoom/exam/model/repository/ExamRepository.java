package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long>, ExamRepositoryCustom {
}
