package com.airoom.airoom.statistic.entity;

import com.airoom.airoom.statistic.entity.value.SummaryType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@EqualsAndHashCode
public class LearningSummaryId implements Serializable {
    private Long lsClassroomStudentNo; //클래스룸 학생 고유번호

    @Enumerated(EnumType.STRING)
    private SummaryType lsType; //통계조회 기준(일별, 주별, 월별)

    private LocalDate lsStartDate; //통계 시작일자
}
