package com.airoom.airoom.textbook.model.dto;

public record SaveDrawingRequest(
        Long unitNo,
        Long classRoomStudentNo,
        String drawingData   // 전체 allDrawings JSON 문자열
) {}
