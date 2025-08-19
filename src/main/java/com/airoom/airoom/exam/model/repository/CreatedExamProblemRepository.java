package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.exam.entity.CreatedExamProblem;
import com.airoom.airoom.exam.model.dto.ExamProblemDetailResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreatedExamProblemRepository extends JpaRepository<CreatedExamProblem, Long> {
    @Query("""
                select new com.airoom.airoom.exam.model.dto.ExamProblemDetailResponse(
                    cep.cepQuestionOrder, cep.cepNo, ep.epNo, ep.epLevel,
                    ep.epQuestion, ep.epImageUrl, ep.epParagraph, ep.epExample,
                    ep.epAnswer, ep.epComment
                )
                from CreatedExamProblem cep
                join cep.examProblem ep
                where cep.exam.examNo = :examNo
                order by cep.cepQuestionOrder
            """)
    List<ExamProblemDetailResponse> findCreatedExamProblemsByExamNo(Long examNo);
}
