package com.airoom.airoom.statistic.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UnitSummaryId implements Serializable {
    private Long usClassroomStudentNo; // 클래스룸 학생 고유번호

    private Long usUnitNo; // 단원 고유번호

    private LocalDate usStartDate; // 시작일
}

