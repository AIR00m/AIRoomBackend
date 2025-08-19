package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.exam.entity.StudentExam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentExamRepository extends JpaRepository<StudentExam, Long> {
}
