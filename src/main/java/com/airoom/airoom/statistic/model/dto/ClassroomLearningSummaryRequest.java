package com.airoom.airoom.statistic.model.dto;

import com.airoom.airoom.statistic.entity.value.SummaryType;

import java.time.LocalDate;

public record ClassroomLearningSummaryRequest(
        Long classroomNo, //클래스룸 고유번호
        SummaryType lsType, //통계 조회기준(일별, 월별)
        LocalDate lsStartDate, //통계 조회 시작일
        LocalDate lsEndDate //통계 조회 종료일
) {
}
