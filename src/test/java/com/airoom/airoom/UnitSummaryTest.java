package com.airoom.airoom;

import com.airoom.airoom.statistic.entity.UnitSummary;
import com.airoom.airoom.statistic.entity.UnitSummaryId;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.repository.UnitSummaryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
public class UnitSummaryTest {

    @Autowired
    private UnitSummaryRepository unitSummaryRepository;

    @Test
    @Transactional
    @Rollback
    void saveDummyUnitSummaries() {
        // 학생번호 고정
        Long classroomStudentNo = 1L;

        // 단원 1 ~ 4 더미 데이터
        for (long unitNo = 1; unitNo <= 4; unitNo++) {
            UnitSummaryId id = new UnitSummaryId(
                    classroomStudentNo,
                    unitNo,
                    SummaryType.DAILY,         // DAILY 고정
                    LocalDate.of(2025, 8, (int) unitNo + 1) // 시작일 다르게
            );

            UnitSummary summary = UnitSummary.builder()
                    .id(id)
                    .usEndDate(LocalDate.of(2025, 8, (int) unitNo + 1)) // 종료일 = 시작일
                    .usTotalLearningDays((int) unitNo)              // 단원 번호에 맞춰 증가
                    .usTotalLearningTimeMs(1000L * unitNo)          // 1000ms 단위로 증가
                    .usTotalProblemsSolved(5 * (int) unitNo)        // 단원 번호 * 5
                    .usTotalCorrectProblems(3 * (int) unitNo)       // 단원 번호 * 3
                    .usAccuracyRate(new BigDecimal("0.75"))         // 고정값
                    .build();

            unitSummaryRepository.save(summary);
        }
    }
}
