package com.airoom.airoom.board.model.dto.homework;

import com.airoom.airoom.board.BoardType;

public record TeacherHomeworkRequest(
        Long memberNo,
        Integer homeworkScore
)
{ }
