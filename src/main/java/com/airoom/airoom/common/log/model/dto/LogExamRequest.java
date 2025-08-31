package com.airoom.airoom.common.log.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LogExamRequest(
        Long examNo, //시험 번호
        Long classroomStudentNo, //학생 번호
        String llType,
        LocalDateTime llStartTime,
        LocalDateTime llEndTime,
        List<ProblemsLogDataRequest> problemsData
) {
    }