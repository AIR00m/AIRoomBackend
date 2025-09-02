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
public class UnitSummaryAggregationTasklet implements Tasklet {
    private final JdbcTemplate jdbc;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Map<String, Object> params = chunkContext.getStepContext().getJobParameters();

        String summaryType = Objects.toString(params.get("summaryType"), "DAILY"); // DAILY or MONTHLY
        LocalDate targetDate = LocalDate.parse(
                Objects.toString(params.get("targetDate"), LocalDate.now().toString())
        );

        final LocalDate startDate;
        final LocalDate endDateInclusive;
        if ("MONTHLY".equalsIgnoreCase(summaryType)) {
            YearMonth ym = YearMonth.from(targetDate);
            startDate = ym.atDay(1);
            endDateInclusive = ym.atEndOfMonth();
        } else {
            startDate = targetDate;
            endDateInclusive = targetDate;
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDateInclusive.plusDays(1).atStartOfDay();

        String sql = """
                INSERT INTO airoom.unit_summary
                (us_classroom_student_no, us_unit_no, us_type, us_start_date, us_end_date,
                 us_total_learning_days, us_total_learning_time_ms,
                 us_total_problems_solved, us_total_correct_problems, us_accuracy_rate,
                 created_at, updated_at)
                SELECT
                    l.classroom_student_no                                   AS cs_no,
                    l.unit_no                                                AS unit_no,
                    ?                                                        AS us_type,
                    ?                                                        AS us_start_date,
                    ?                                                        AS us_end_date,
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
                WHERE l.unit_no IS NOT NULL
                  AND l.ll_start_time >= ?
                  AND l.ll_start_time <  ?
                GROUP BY l.classroom_student_no, l.unit_no
                ON DUPLICATE KEY UPDATE
                    us_end_date               = VALUES(us_end_date),
                    us_total_learning_days    = VALUES(us_total_learning_days),
                    us_total_learning_time_ms = VALUES(us_total_learning_time_ms),
                    us_total_problems_solved  = VALUES(us_total_problems_solved),
                    us_total_correct_problems = VALUES(us_total_correct_problems),
                    us_accuracy_rate          = VALUES(us_accuracy_rate),
                    updated_at                = VALUES(updated_at);
                """;

        jdbc.update(
                sql,
                summaryType.toUpperCase(),
                startDate,
                endDateInclusive,
                startDateTime,
                endExclusive
        );

        return RepeatStatus.FINISHED;
    }
}
