package com.airoom.airoom.exam.model.dto;

import java.time.Duration;

public record StudentAnswerResponse(
        String unitTitle, //단원명
        Boolean isCorrect, //정답여부
        Integer cepQuestionOrder, //시험출제문항 번호
        Long cepNo, //시험출제문제 고유번호
        Long epNo, //시험문제 고유번호
        Duration saSolvingTime, //풀이시간
        String selectedAnswer, //선택한 답
        String correctAnswer //실제 정답
) {
}
