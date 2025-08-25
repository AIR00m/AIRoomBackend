package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.exam.entity.Exam;
import com.airoom.airoom.exam.entity.StudentAnswer;
import com.airoom.airoom.exam.model.dto.StudentAnswerResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    @Query("""
            select new com.airoom.airoom.exam.model.dto.StudentAnswerResponse(
                sa.examProblem.unit.unitTitle,
                sa.saIsCorrect,
                sa.createdExamProblem.cepQuestionOrder,
                sa.createdExamProblem.cepNo,
                sa.examProblem.epNo,
                sa.saSolvingTime,
                sa.saAnswer,
                sa.examProblem.epAnswer
            )
            from StudentAnswer sa
            join sa.exam e
            join sa.classroomStudent cs
            where e = :exam
            and cs = :classroomStudent
            """)
    List<StudentAnswerResponse> findStudentAnswersByClassroomStudentAndExam(ClassroomStudent classroomStudent, Exam exam);
}
