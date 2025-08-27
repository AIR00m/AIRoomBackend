package com.airoom.airoom;

import com.airoom.airoom.statistic.entity.LearningSummary;
import com.airoom.airoom.statistic.entity.LearningSummaryId;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.repository.LearningSummaryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class LearningSummaryTest {
    @Autowired
    private LearningSummaryRepository learningSummaryRepository;

    /**
     * LearningSummary 더미데이터 생성
     */
    @Test
    @Transactional
    @Rollback
    public void insertDummyLearningSummary() {
        LearningSummaryId id1 = new LearningSummaryId(1L, SummaryType.DAILY, LocalDate.of(2025, 8, 20));
        LearningSummaryId id2 = new LearningSummaryId(1L, SummaryType.DAILY, LocalDate.of(2025, 8, 21));
        LearningSummaryId id3 = new LearningSummaryId(1L, SummaryType.DAILY, LocalDate.of(2025, 8, 22));
        LearningSummaryId id4 = new LearningSummaryId(1L, SummaryType.DAILY, LocalDate.of(2025, 8, 23));

        LearningSummary ls1 = LearningSummary.builder()
                .id(id1)
                .lsEndDate(LocalDate.of(2025, 8, 20))
                .lsTotalLearningDays(1)
                .lsTotalLearningTime(Duration.ofMinutes(90)) // 1시간 30분
                .lsTotalProblemsSolved(20)
                .lsTotalCorrectProblems(15)
                .lsAccuracyRate(BigDecimal.valueOf(75.00))
                .build();

        LearningSummary ls2 = LearningSummary.builder()
                .id(id2)
                .lsEndDate(LocalDate.of(2025, 8, 21))
                .lsTotalLearningDays(1)
                .lsTotalLearningTime(Duration.ofMinutes(60)) // 1시간
                .lsTotalProblemsSolved(10)
                .lsTotalCorrectProblems(8)
                .lsAccuracyRate(BigDecimal.valueOf(80.00))
                .build();

        LearningSummary ls3 = LearningSummary.builder()
                .id(id3)
                .lsEndDate(LocalDate.of(2025, 8, 22))
                .lsTotalLearningDays(1)
                .lsTotalLearningTime(Duration.ofMinutes(120)) // 2시간
                .lsTotalProblemsSolved(15)
                .lsTotalCorrectProblems(10)
                .lsAccuracyRate(BigDecimal.valueOf(66.67))
                .build();

        LearningSummary ls4 = LearningSummary.builder()
                .id(id4)
                .lsEndDate(LocalDate.of(2025, 8, 23))
                .lsTotalLearningDays(1)
                .lsTotalLearningTime(Duration.ofMinutes(45)) // 45분
                .lsTotalProblemsSolved(12)
                .lsTotalCorrectProblems(9)
                .lsAccuracyRate(BigDecimal.valueOf(75.00))
                .build();

        LearningSummaryId id5 = new LearningSummaryId(2L, SummaryType.DAILY, LocalDate.of(2025, 8, 20));
        LearningSummaryId id6 = new LearningSummaryId(2L, SummaryType.DAILY, LocalDate.of(2025, 8, 21));
        LearningSummaryId id7 = new LearningSummaryId(2L, SummaryType.DAILY, LocalDate.of(2025, 8, 22));
        LearningSummaryId id8 = new LearningSummaryId(2L, SummaryType.DAILY, LocalDate.of(2025, 8, 23));

        LearningSummary ls5 = LearningSummary.builder()
                .id(id5)
                .lsEndDate(LocalDate.of(2025, 8, 20))
                .lsTotalLearningDays(1)
                .lsTotalLearningTime(Duration.ofMinutes(80)) // 1시간 20분
                .lsTotalProblemsSolved(18)
                .lsTotalCorrectProblems(14)
                .lsAccuracyRate(BigDecimal.valueOf(77.78))
                .build();

        LearningSummary ls6 = LearningSummary.builder()
                .id(id6)
                .lsEndDate(LocalDate.of(2025, 8, 21))
                .lsTotalLearningDays(1)
                .lsTotalLearningTime(Duration.ofMinutes(95)) // 1시간 35분
                .lsTotalProblemsSolved(22)
                .lsTotalCorrectProblems(17)
                .lsAccuracyRate(BigDecimal.valueOf(77.27))
                .build();

        LearningSummary ls7 = LearningSummary.builder()
                .id(id7)
                .lsEndDate(LocalDate.of(2025, 8, 22))
                .lsTotalLearningDays(1)
                .lsTotalLearningTime(Duration.ofMinutes(70)) // 1시간 10분
                .lsTotalProblemsSolved(16)
                .lsTotalCorrectProblems(12)
                .lsAccuracyRate(BigDecimal.valueOf(75.00))
                .build();

        LearningSummary ls8 = LearningSummary.builder()
                .id(id8)
                .lsEndDate(LocalDate.of(2025, 8, 23))
                .lsTotalLearningDays(1)
                .lsTotalLearningTime(Duration.ofMinutes(55)) // 55분
                .lsTotalProblemsSolved(14)
                .lsTotalCorrectProblems(9)
                .lsAccuracyRate(BigDecimal.valueOf(64.29))
                .build();

        learningSummaryRepository.saveAll(List.of(ls1, ls2, ls3, ls4, ls5, ls6, ls7, ls8));
    }
}
