package com.airoom.airoom.exam.model.dto;

import com.airoom.airoom.exam.entity.value.ProblemLevel;

public record ExamProblemDetailResponse(
        Integer questionOrder, //시험출제문제 문항번호
        Long cepNo, //시험출제문제 고유번호
        Long epNo, //시험문제 고유번호
        ProblemLevel epLevel, //시험문제 난이도 ex) 상,중,하
        String epQuestion, //시험문제 문항
        String epImageUrl, //시험문제 이미지
        String epParagraph, //시험문제 지문
        String epExample, //시험문제 보기
        String epAnswer, //시험문제 정답
        String epComment //시험문제 해설
) {
}
