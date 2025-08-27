package com.airoom.airoom.statistic.entity;

import com.airoom.airoom.statistic.entity.value.SummaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private Long usUnitNo; //단원 고유번호

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private SummaryType usType; // DAILY, MONTHLY

    private LocalDate usStartDate; // 시작일
}

