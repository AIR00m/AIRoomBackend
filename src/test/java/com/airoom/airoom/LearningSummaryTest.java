package com.airoom.airoom;

import com.airoom.airoom.statistic.entity.LearningSummary;
import com.airoom.airoom.statistic.entity.LearningSummaryId;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.repository.LearningSummaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

        learningSummaryRepository.saveAll(List.of(ls1, ls2, ls3, ls4));
    }
}
