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
public class LearningSummaryAggregationTasklet implements Tasklet {
    private final JdbcTemplate jdbc;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
        Map<String, Object> params = context.getStepContext().getJobParameters();

        String summaryType = Objects.toString(params.get("summaryType")); //DAILY, MONTHLY
        LocalDate targetDate = LocalDate.parse(Objects.toString(params.get("targetDate"))); //기준일

        //집계 구간 결정
        LocalDate fromDate;
        LocalDate toDate;
        if ("MONTHLY".equalsIgnoreCase(summaryType)) {
            YearMonth ym = YearMonth.from(targetDate);
            fromDate = ym.atDay(1);
            toDate = ym.atEndOfMonth();
        } else {
            fromDate = targetDate;
            toDate = targetDate;
        }

        String sql = """
                INSERT INTO learning_summary (
                    ls_classroom_student_no, ls_start_date, ls_type,
                    ls_end_date, ls_total_learning_days, ls_total_learning_time,
                    ls_total_problems_solved, ls_total_correct_problems, ls_accuracy_rate,
                    created_at, updated_at
                )
                SELECT
                  t.ls_classroom_student_no,
                  ?, ?, ?,
                  t.days, t.time_ms,
                  t.solved, t.correct,
                  COALESCE(ROUND(100.0 * t.correct / NULLIF(t.solved, 0), 2), 0) AS ls_accuracy_rate,
                  NOW(), NOW()
                FROM (
                  SELECT
                    ll.classroom_student_no                             AS ls_classroom_student_no,
                    COUNT(DISTINCT DATE(ll.ll_start_time))              AS days,
                    SUM(ll.ll_duration_sec * 1000)                      AS time_ms,
                    SUM(CASE WHEN ll.ll_type='EXAM' THEN 1 ELSE 0 END)  AS solved,
                    SUM(CASE WHEN ll.ll_type='EXAM' AND ll.ll_is_correct = 1 THEN 1 ELSE 0 END) AS correct
                  FROM learning_log ll
                  WHERE ll.ll_start_time >= ? AND ll.ll_start_time < ?
                  GROUP BY ll.classroom_student_no
                ) AS t
                ON DUPLICATE KEY UPDATE
                  ls_end_date               = VALUES(ls_end_date),
                  ls_total_learning_days    = VALUES(ls_total_learning_days),
                  ls_total_learning_time    = VALUES(ls_total_learning_time),
                  ls_total_problems_solved  = VALUES(ls_total_problems_solved),
                  ls_total_correct_problems = VALUES(ls_total_correct_problems),
                  ls_accuracy_rate          = VALUES(ls_accuracy_rate),
                  updated_at                = VALUES(updated_at);
                """;

        jdbc.update(sql, fromDate, summaryType.toUpperCase(), toDate, fromDate, toDate);

        return RepeatStatus.FINISHED;
    }
}
