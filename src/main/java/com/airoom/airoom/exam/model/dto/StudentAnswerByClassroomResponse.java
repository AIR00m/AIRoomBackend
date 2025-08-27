package com.airoom.airoom.exam.model.dto;

import java.util.List;

public record StudentAnswerByClassroomResponse(
        Long classroomStudentNo, //클래스룸 학생 고유번호
        String classroomStudentName, //클래스룸 학생이름
        List<StudentAnswerResponse> studentAnswerResponseList //시험별 학생 정답 리스트
) {
}
