package com.airoom.airoom.classroom.model.dto;

public record ClassroomStudentResponse(
    Long classroomStudentNo, //클래스룸 학생 고유번호
    String studentName, //클래스룸 학생명
    String studentId //클래스룸 학생 아이디
) {
}
