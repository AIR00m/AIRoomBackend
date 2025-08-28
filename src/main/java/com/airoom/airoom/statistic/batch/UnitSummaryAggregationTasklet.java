package com.airoom.airoom.statistic.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UnitSummaryAggregationTasklet implements Tasklet {
    private final JdbcTemplate jdbc;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
        Map<String, Object> params = context.getStepContext().getJobParameters();

        String summaryType = Objects.toString(params.get("summaryType")); // "DAILY" | "MONTHLY"
        LocalDate targetDate = LocalDate.parse(Objects.toString(params.get("targetDate")));

        LocalDate fromDate;
        LocalDate toDate;
        if ("MONTHLY".equalsIgnoreCase(summaryType)) {
            var ym = YearMonth.from(targetDate);
            fromDate = ym.atDay(1);
            toDate   = ym.atEndOfMonth();
        } else {
            fromDate = targetDate;
            toDate   = targetDate;
        }

        String sql = """
            INSERT INTO unit_summary (
              us_classroom_student_no, us_start_date, us_type, us_unit_no,
              us_end_date, us_total_learning_days, us_total_learning_time_ms,
              us_total_problems_solved, us_total_correct_problems, us_accuracy_rate,
              created_at, updated_at
            )
            SELECT
              t.us_classroom_student_no,
              ?, ?, t.us_unit_no, ?,
              t.days, t.time_ms,
              t.solved, t.correct,
              COALESCE(ROUND(100.0 * t.correct / NULLIF(t.solved, 0), 2), 0),
              NOW(), NOW()
            FROM (
              SELECT
                ll.classroom_student_no                                    AS us_classroom_student_no,
                ll.unit_no                                                 AS us_unit_no,
                COUNT(DISTINCT DATE(ll.ll_start_time))                     AS days,
                SUM(ll.ll_duration_sec * 1000)                             AS time_ms,
                SUM(CASE WHEN ll.ll_type = 'EXAM' THEN 1 ELSE 0 END)       AS solved,
                SUM(CASE WHEN ll.ll_type = 'EXAM' AND ll.ll_is_correct=1
                         THEN 1 ELSE 0 END)                                AS correct
              FROM learning_log ll
              WHERE ll.unit_no IS NOT NULL
                AND ll.ll_start_time >= ? AND ll.ll_start_time < ?
              GROUP BY ll.classroom_student_no, ll.unit_no
            ) AS t
            ON DUPLICATE KEY UPDATE
              us_end_date               = VALUES(us_end_date),
              us_total_learning_days    = VALUES(us_total_learning_days),
              us_total_learning_time_ms = VALUES(us_total_learning_time_ms),
              us_total_problems_solved  = VALUES(us_total_problems_solved),
              us_total_correct_problems = VALUES(us_total_correct_problems),
              us_accuracy_rate          = VALUES(us_accuracy_rate),
              updated_at                = VALUES(updated_at);
            """;

        jdbc.update(sql,
                fromDate, summaryType.toUpperCase(), toDate,
                fromDate, toDate
        );

        return RepeatStatus.FINISHED;
    }
}
