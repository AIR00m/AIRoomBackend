package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.exam.entity.Exam;
import com.airoom.airoom.exam.entity.StudentExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentExamRepository extends JpaRepository<StudentExam, Long> {
    StudentExam findStudentExamByClassroomStudentAndExam(ClassroomStudent classroomStudent, Exam exam);
}
