package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.exam.entity.ExamProblem;
import com.airoom.airoom.exam.entity.value.ProblemLevel;
import com.airoom.airoom.exam.model.dto.ExamProblemResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamProblemRepository extends JpaRepository<ExamProblem, Long> {
    @Query("""
            select new com.airoom.airoom.exam.model.dto.ExamProblemResponse(
                ep.epNo, ep.epLevel, ep.epQuestion, ep.epImageUrl, ep.epParagraph,
                ep.epExample, ep.epAnswer, ep.epComment,
                u.unitNum, u.unitTitle
            )
            from ExamProblem ep
            join ep.unit u
            where u.unitNo = :unitNo
              and ep.epLevel = :level
              and ep.deletedAt is null
            order by function('RAND')
            """)
    List<ExamProblemResponse> findRandomExamProblemByUnitAndLevel(
            @Param("unitNo") Long unitNo,
            @Param("level") ProblemLevel level,
            Pageable pageable
    );
}
