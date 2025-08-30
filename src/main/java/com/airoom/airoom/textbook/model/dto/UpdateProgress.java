package com.airoom.airoom.textbook.model.dto;

public record UpdateProgress(    Long classRoomStudentNo,
                                 Long unitNo,
                                 Integer progressLastPage) {
}
