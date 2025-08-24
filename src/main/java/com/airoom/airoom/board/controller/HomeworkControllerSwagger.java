package com.airoom.airoom.board.controller;

import com.airoom.airoom.board.model.dto.homework.TeacherHomeworkRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Homework(과제 제출) 관련 API", description = "Homework 관련 API")
public interface HomeworkControllerSwagger {
    @Operation(
            summary = "과제제출 점수 등록 API",
            description = "선생님이 해당하는 과제 점수를 등록"
    )
    public ResponseEntity<Void> saveStudentHomeworkScores
            (Long boardNo, List<TeacherHomeworkRequest> request);
}
