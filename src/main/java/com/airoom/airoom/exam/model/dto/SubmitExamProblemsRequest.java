package com.airoom.airoom.exam.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record SubmitExamProblemsRequest(
        @NotNull(message = "클래스룸 학생 고유번호는 필수입니다.")
        Long classroomStudentNo,

        @NotNull(message = "시험 고유번호는 필수입니다.")
        Long examNo,

        LocalDateTime seStartTime,

        LocalDateTime seEndTime,

        @NotEmpty(message = "학생 응답은 필수입니다.")
        List<@Valid StudentAnswerRequest> studentAnswerRequestList
) {
}
