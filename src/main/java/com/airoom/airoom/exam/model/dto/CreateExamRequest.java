package com.airoom.airoom.exam.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record CreateExamRequest(
        @NotNull(message = "시험 이름은 필수입니다.")
        String examName,

        @NotNull(message = "시험 범위는 필수입니다.")
        List<Long> unitNoList,

        @NotNull(message = "시험 문제 수는 필수입니다.")
        Integer examProblemCount,

        LocalDateTime examStartTime,

        LocalDateTime examEndTime,

        @NotEmpty(message = "시험문제는 필수 항목입니다.")
        List<Long> epNoList,

        @NotNull(message = "클래스룸 고유번호는 필수입니다.")
        Long classroomNo,

        @NotEmpty(message = "클래스룸 학생 고유번호는 필수입니다.")
        List<Long> classroomStudentNoList
) {
}
