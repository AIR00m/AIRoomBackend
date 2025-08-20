package com.airoom.airoom.exam.model.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record SubmitExamProblemsResponse(
        String examName, //시험명
        Duration totalProblemSolvingTime, //총 풀이시간
        LocalDateTime examinationDate, //응시일시
        Integer totalScore, //시험점수
        List<StudentAnswerResponse> studentAnswerResponseList //시험 항목에 대한 정보 (단원명, 정답여부, 시험문항번호, 시험출제문제고유번호, 시험문제 고유번호, 항목별 풀이시간, 선택한답, 실제정답)
) {
}
