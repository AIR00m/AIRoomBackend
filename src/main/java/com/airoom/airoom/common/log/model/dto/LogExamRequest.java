package com.airoom.airoom.common.log.model.dto;

public record LogExamRequest(
    Long examNo, //시험 번호
    Long classroomStudentNo, //학생 번호
    Long timestamp, // 현재 시간
    Long solvingTime, // 문제 풀이 시간
    Integer problemNo, //문제 번호
    Integer controlVCount,
    Integer controlCCount,
    Integer afkCount,
    Integer devToolsCount,
    Integer rightClickCount,
    Integer focusLossCount,
    Integer tabSwitchCount
) {
    }