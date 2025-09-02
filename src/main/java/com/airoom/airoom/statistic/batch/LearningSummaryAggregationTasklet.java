package com.airoom.airoom.statistic.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LearningSummaryAggregationTasklet implements Tasklet {
    private final JdbcTemplate jdbc;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Map<String, Object> params = chunkContext.getStepContext().getJobParameters();

        String summaryType = Objects.toString(params.get("summaryType"), "DAILY"); // DAILY or MONTHLY
        LocalDate targetDate = LocalDate.parse(
                Objects.toString(params.get("targetDate"), LocalDate.now().toString())
        );

        // 날짜 경계 계산
        final LocalDate startDate;         // 요약의 시작일(포함)
        final LocalDate endDateInclusive;  // 요약의 종료일(포함)
        if ("MONTHLY".equalsIgnoreCase(summaryType)) {
            YearMonth ym = YearMonth.from(targetDate);
            startDate = ym.atDay(1);
            endDateInclusive = ym.atEndOfMonth();
        } else {
            startDate = targetDate;
            endDateInclusive = targetDate;
        }

        // WHERE 절에는 [startOfDay, nextStartOfDay) 사용 (닫힌 구간 문제/타임존 엣지 방지)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDateInclusive.plusDays(1).atStartOfDay();

        // 집계 SQL (airoom 스키마 명시)
        String sql = """
                INSERT INTO airoom.learning_summary
                (ls_classroom_student_no, ls_type, ls_start_date, ls_end_date,
                 ls_total_learning_days, ls_total_learning_time,
                 ls_total_problems_solved, ls_total_correct_problems, ls_accuracy_rate,
                 created_at, updated_at)
                SELECT
                    l.classroom_student_no                                   AS cs_no,
                    ?                                                        AS ls_type,
                    ?                                                        AS ls_start_date,
                    ?                                                        AS ls_end_date,
                    COUNT(DISTINCT DATE(l.ll_start_time))                    AS days,
                    SUM(l.ll_duration_ms) * 1000                            AS total_time_ms,
                    SUM(CASE WHEN l.ll_is_correct IS NOT NULL THEN 1 ELSE 0 END) AS solved,
                    SUM(CASE WHEN l.ll_is_correct = b'1' THEN 1 ELSE 0 END)  AS correct,
                    CASE 
                        WHEN SUM(CASE WHEN l.ll_is_correct IS NOT NULL THEN 1 ELSE 0 END) > 0
                        THEN ROUND(
                            SUM(CASE WHEN l.ll_is_correct = b'1' THEN 1 ELSE 0 END) * 100.0
                            / SUM(CASE WHEN l.ll_is_correct IS NOT NULL THEN 1 ELSE 0 END), 2
                        )
                        ELSE 0
                    END                                                      AS accuracy,
                    NOW(), NOW()
                FROM airoom.learning_log l
                WHERE l.ll_start_time >= ?
                  AND l.ll_start_time <  ?
                GROUP BY l.classroom_student_no
                ON DUPLICATE KEY UPDATE
                    ls_end_date               = VALUES(ls_end_date),
                    ls_total_learning_days    = VALUES(ls_total_learning_days),
                    ls_total_learning_time    = VALUES(ls_total_learning_time),
                    ls_total_problems_solved  = VALUES(ls_total_problems_solved),
                    ls_total_correct_problems = VALUES(ls_total_correct_problems),
                    ls_accuracy_rate          = VALUES(ls_accuracy_rate),
                    updated_at                = VALUES(updated_at);
                """;

        jdbc.update(
                sql,
                summaryType.toUpperCase(),    // ?
                startDate,                    // ?
                endDateInclusive,             // ?
                startDateTime,                // ?
                endExclusive                  // ?
        );

        return RepeatStatus.FINISHED;
    }
}
