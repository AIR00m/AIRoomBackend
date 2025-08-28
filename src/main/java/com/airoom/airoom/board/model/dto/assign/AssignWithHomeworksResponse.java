package com.airoom.airoom.board.model.dto.assign;

import com.airoom.airoom.board.model.dto.homework.StudentHomeworkResponse;

import java.util.List;

public record AssignWithHomeworksResponse (
        AssignTeacherResponse assignTeacherResponse,
        List<StudentHomeworkResponse> studentHomeworkResponses
){
}
