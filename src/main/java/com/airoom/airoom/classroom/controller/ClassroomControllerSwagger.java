package com.airoom.airoom.classroom.controller;

import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Classroom 관련 API", description = "Classroom 관련 API")
public interface ClassroomControllerSwagger {
    @Operation(
            summary = "클래스룸 학생 전체 조회 API",
            description = "클래스룸 학생을 전체 조회합니다."
    )
    public List<ClassroomStudentResponse> getClassroomStudentAll(
            @PathVariable Long classroomNo
    );
}
