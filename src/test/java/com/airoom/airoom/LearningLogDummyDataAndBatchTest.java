package com.airoom.airoom;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LearningLogDummyDataAndBatchTest {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    @Qualifier("learningAggregationJob")
    Job learningAggregationJob;

    // ====== 시딩(더미) 설정 ======
    // 최근 40일 ~ 어제까지
    private final LocalDate fromDate = LocalDate.now().minusDays(40);
    private final LocalDate toDate   = LocalDate.now().minusDays(1);

    // FK 범위 설정
    private long pickClassroomStudentNo() { return ThreadLocalRandom.current().nextLong(1, 6 + 1); } // 1~6
    private long pickUnitNo()             { return ThreadLocalRandom.current().nextLong(1, 5 + 1); } // 1~5
    private long pickCepNo()              { return ThreadLocalRandom.current().nextLong(1, 71 + 1);} // 1~71

    // 시작 시각: 06:00 ~ 22:59
    private LocalDateTime pickStart(LocalDate day) {
        int plusMinutes = ThreadLocalRandom.current().nextInt(0, (17 * 60)); // 06:00 + [0..1019]분
        return LocalDateTime.of(day, LocalTime.of(6, 0)).plusMinutes(plusMinutes);
    }

    // 학습 길이: 15~60분
    private int pickDurationMinutes() {
        return ThreadLocalRandom.current().nextInt(15, 61);
    }

    // 선택지
    private String pickSelectedAnswer() {
        char[] c = {'A','B','C','D'};
        return String.valueOf(c[ThreadLocalRandom.current().nextInt(c.length)]);
    }

    @Test
    @Transactional
    @Rollback
    void seedAndRunDailyThenMonthly() throws Exception {
        // =========================
        // 1) 더미 데이터 시딩
        // =========================
        jdbc.update("DELETE FROM airoom.learning_log");

        LocalDate cur = fromDate;
        while (!cur.isAfter(toDate)) {
            for (long csNo = 1; csNo <= 6; csNo++) {
                // ====== LEARN 로그 ======
                long unitNo = pickUnitNo();
                long cepNo  = pickCepNo();

                LocalDateTime start = pickStart(cur);
                int durationMin = pickDurationMinutes();
                LocalDateTime end = start.plusMinutes(durationMin);
                long durationMs = ChronoUnit.MILLIS.between(start, end);

                jdbc.update("""
                    INSERT INTO airoom.learning_log
                      (ll_duration_ms, ll_end_time, ll_is_correct, ll_start_time, ll_type,
                       classroom_student_no, cep_no, unit_no, selected_answer, anomaly_count)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                        durationMs,
                        Timestamp.valueOf(end),
                        ThreadLocalRandom.current().nextBoolean() ? 1 : 0,
                        Timestamp.valueOf(start),
                        "LEARN",
                        csNo,
                        cepNo,
                        unitNo,
                        pickSelectedAnswer(),
                        ThreadLocalRandom.current().nextInt(0, 4) // 0~3 랜덤
                );

                // ====== EXAM 로그 ======
                unitNo = pickUnitNo();
                cepNo  = pickCepNo();

                start = pickStart(cur).plusMinutes(1); // 겹치지 않게 살짝 밀어줌
                durationMin = pickDurationMinutes();
                end = start.plusMinutes(durationMin);
                durationMs = ChronoUnit.MILLIS.between(start, end);

                jdbc.update("""
                    INSERT INTO airoom.learning_log
                      (ll_duration_ms, ll_end_time, ll_is_correct, ll_start_time, ll_type,
                       classroom_student_no, cep_no, unit_no, selected_answer, anomaly_count)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                        durationMs,
                        Timestamp.valueOf(end),
                        ThreadLocalRandom.current().nextBoolean() ? 1 : 0,
                        Timestamp.valueOf(start),
                        "EXAM",
                        csNo,
                        cepNo,
                        unitNo,
                        pickSelectedAnswer(),
                        ThreadLocalRandom.current().nextInt(0, 4)
                );
            }
            cur = cur.plusDays(1);
        }

        // =========================
        // 2) DAILY 집계 (fromDate ~ toDate)
        // =========================
        LocalDate dailyCur = fromDate;
        while (!dailyCur.isAfter(toDate)) {
            JobParameters dailyParams = new JobParametersBuilder()
                    .addString("summaryType", "DAILY")
                    .addString("targetDate", dailyCur.toString())
                    .addLong("run.id", System.nanoTime())
                    .toJobParameters();

            JobExecution dailyExec = jobLauncher.run(learningAggregationJob, dailyParams);
            assertThat(dailyExec.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

            dailyCur = dailyCur.plusDays(1);
        }

        // =========================
        // 3) MONTHLY 집계 (fromDate ~ toDate)
        // =========================
        LocalDate monthlyTarget = fromDate.withDayOfMonth(1);
        while (!monthlyTarget.isAfter(toDate)) {
            JobParameters monthlyParams = new JobParametersBuilder()
                    .addString("summaryType", "MONTHLY")
                    .addString("targetDate", monthlyTarget.toString())
                    .addLong("run.id", System.nanoTime())
                    .toJobParameters();

            JobExecution monthlyExec = jobLauncher.run(learningAggregationJob, monthlyParams);
            assertThat(monthlyExec.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

            monthlyTarget = monthlyTarget.plusMonths(1);
        }
    }
}
