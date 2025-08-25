package com.airoom.airoom.board.model.dto.homework;


import com.airoom.airoom.board.entity.BoardType;

public record StudentHomeworkRequest(
        Long assignBoardNo,
        BoardType boardType
)
{ }
