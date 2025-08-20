package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.exam.entity.ExamProblem;
import com.airoom.airoom.exam.entity.value.ProblemLevel;
import com.airoom.airoom.exam.model.dto.ExamProblemResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
    List<ExamProblemResponse> findRandomExamProblemsByUnitAndLevel(
            @Param("unitNo") Long unitNo,
            @Param("level") ProblemLevel level,
            Pageable pageable
    );

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
                  and ep.epNo <> :excludeEpNo
                order by function('RAND')
            """)
    List<ExamProblemResponse> findRandomByUnitAndLevelExcludingSelf( //자기 자신을 제외한 랜덤문제 생성
                                                                     @Param("unitNo") Long unitNo,
                                                                     @Param("level") ProblemLevel level,
                                                                     @Param("excludeEpNo") Long excludeEpNo,
                                                                     Pageable pageable
    );

    //ep.getUnit().getUnitTitle()시에 N+1 방지
    //N+1 방지를 위해 fetch join도 가능
    @EntityGraph(attributePaths = "unit")
    List<ExamProblem> findExamProblemsByEpNoIn(Collection<Long> epNos);
}
