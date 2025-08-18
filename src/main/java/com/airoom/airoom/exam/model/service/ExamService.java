package com.airoom.airoom.exam.model.service;

import com.airoom.airoom.exam.entity.value.ProblemLevel;
import com.airoom.airoom.exam.model.dto.CreateExamProblemsRequest;
import com.airoom.airoom.exam.model.dto.CreateExamProblemsResponse;
import com.airoom.airoom.exam.model.dto.ExamProblemRequest;
import com.airoom.airoom.exam.model.dto.ExamProblemResponse;
import com.airoom.airoom.exam.model.repository.ExamProblemRepository;
import com.airoom.airoom.exam.model.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final ExamProblemRepository examProblemRepository;


    /**
     * 난이도, 단원별 랜덤 문제 출제
     */
    @Transactional(readOnly = true)
    public CreateExamProblemsResponse getExamProblemsByLevelAndUnit(final CreateExamProblemsRequest request) {
        List<ExamProblemResponse> examProblemResponseList = new ArrayList<>();
        for (ExamProblemRequest examProblemRequest : request.examProblemRequestList()) {
            Long unitNo = examProblemRequest.unitNo();
            for (Map.Entry<ProblemLevel, Integer> entry : examProblemRequest.problemCountsByLevel().entrySet()) {
                ProblemLevel problemLevel = entry.getKey();
                Integer count = entry.getValue();
                examProblemResponseList.addAll(examProblemRepository.findRandomExamProblemByUnitAndLevel(unitNo, problemLevel, PageRequest.of(0, count)));
            }
        }
        return new CreateExamProblemsResponse(examProblemResponseList);
    }
}
