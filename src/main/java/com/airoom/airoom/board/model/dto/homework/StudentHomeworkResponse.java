package com.airoom.airoom.board.model.dto.homework;

import java.time.LocalDateTime;

public record StudentHomeworkResponse
        (
                String memberName,
                Boolean homeworkSubmitType,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                String originalName,
                String s3key,
                Integer homeworkScore
        )
{ }
