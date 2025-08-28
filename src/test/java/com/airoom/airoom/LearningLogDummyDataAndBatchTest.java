package com.airoom.airoom;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

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
    @Qualifier("learningAggregationJob") // 잡 빈 이름이 다르면 여기만 변경
    Job learningAggregationJob;

    // ====== 시딩(더미) 설정 ======
    // 최근 40일 ~ 어제까지
    private final LocalDate fromDate = LocalDate.now().minusDays(40);
    private final LocalDate toDate   = LocalDate.now().minusDays(1);

    // 하루에 학생 1명당 생성할 로그 개수 범위
    private final int minLogsPerDayPerStudent = 5;
    private final int maxLogsPerDayPerStudent = 12;

    // 고정 조건: classroom_student_no ∈ [1..2], unit_no ∈ [1..5], cep_no ∈ [1..71]
    private long pickClassroomStudentNo() { return ThreadLocalRandom.current().nextLong(1, 2 + 1); }
    private long pickUnitNo()             { return ThreadLocalRandom.current().nextLong(1, 5 + 1); }
    private long pickCepNo()              { return ThreadLocalRandom.current().nextLong(1, 71 + 1); }

    // 시작 시각: 06:00 ~ 22:59, 학습 길이: 5~120분
    private LocalDateTime pickStart(LocalDate day) {
        int plusMinutes = ThreadLocalRandom.current().nextInt(0, (17 * 60)); // 06:00 + [0..1019]분
        return LocalDateTime.of(day, LocalTime.of(6, 0)).plusMinutes(plusMinutes);
    }
    private int pickDurationMinutes() { return ThreadLocalRandom.current().nextInt(5, 120 + 1); }

    // 타입 & 선택지
    private String pickType() { return ThreadLocalRandom.current().nextBoolean() ? "EXAM" : "LEARN"; } // ENUM('EXAM','LEARN')
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
        // FK 주의: 아래 ID 범위의 레코드가 실제로 존재해야 INSERT 성공함
        //  - airoom.classroom_student.class_room_student_no: 1,2
        //  - airoom.unit.unit_no: 1..5
        //  - airoom.created_exam_problem.cep_no: 1..71
        // 필요시 안전하게 비우고 시작하려면 아래 주석 해제 (운영 DB 금지)
        // jdbc.update("DELETE FROM airoom.learning_log");

        LocalDate cur = fromDate;
        while (!cur.isAfter(toDate)) {
            for (long csNo = 1; csNo <= 2; csNo++) {
                int logsToday = ThreadLocalRandom.current()
                        .nextInt(minLogsPerDayPerStudent, maxLogsPerDayPerStudent + 1);

                for (int i = 0; i < logsToday; i++) {
                    long unitNo = pickUnitNo();
                    long cepNo  = pickCepNo();

                    LocalDateTime start = pickStart(cur);
                    int durationMin = pickDurationMinutes();
                    LocalDateTime end = start.plusMinutes(durationMin);

                    long durationSec = ChronoUnit.SECONDS.between(start, end);
                    String type = pickType();

                    // ll_is_correct: EXAM은 true/false 랜덤, LEARN은 70% 확률로 값, 30% NULL
                    Integer isCorrectBit = null; // 0/1/NULL (MySQL BIT 컬럼)
                    String selectedAnswer = null;

                    if ("EXAM".equals(type)) {
                        boolean correct = ThreadLocalRandom.current().nextBoolean();
                        isCorrectBit = correct ? 1 : 0;
                        selectedAnswer = pickSelectedAnswer();
                    } else {
                        if (ThreadLocalRandom.current().nextDouble() < 0.7) {
                            isCorrectBit = ThreadLocalRandom.current().nextBoolean() ? 1 : 0;
                            selectedAnswer = pickSelectedAnswer();
                        } else {
                            isCorrectBit = null;
                            selectedAnswer = null;
                        }
                    }

                    // DDL에 정확히 맞춘 INSERT
                    jdbc.update("""
                        INSERT INTO airoom.learning_log
                          (ll_duration_sec, ll_end_time, ll_is_correct, ll_start_time, ll_type,
                           classroom_student_no, cep_no, unit_no, selected_answer)
                        VALUES
                          (?, ?, ?, ?, ?,
                           ?, ?, ?, ?)
                    """,
                            durationSec,
                            Timestamp.valueOf(end),
                            isCorrectBit, // BIT: 0/1/NULL
                            Timestamp.valueOf(start),
                            type,         // ENUM('EXAM','LEARN')
                            csNo,
                            cepNo,
                            unitNo,
                            selectedAnswer
                    );
                }
            }
            cur = cur.plusDays(1);
        }

        // =========================
        // 2) 스프링 배치 - 일일 집계
        // =========================
        LocalDate dailyTarget = toDate; // 어제
        JobParameters dailyParams = new JobParametersBuilder()
                .addString("summaryType", "DAILY")
                .addString("targetDate", dailyTarget.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution dailyExec = jobLauncher.run(learningAggregationJob, dailyParams);
        assertThat(dailyExec.getExitStatus()).as("DAILY aggregation should complete")
                .isEqualTo(ExitStatus.COMPLETED);

        // =========================
        // 3) 스프링 배치 - 월간 집계
        // =========================
        LocalDate monthlyAnyDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(15);
        JobParameters monthlyParams = new JobParametersBuilder()
                .addString("summaryType", "MONTHLY")
                .addString("targetDate", monthlyAnyDayOfLastMonth.toString())
                .addLong("run.id", System.currentTimeMillis() + 1) // run.id 다르게
                .toJobParameters();

        JobExecution monthlyExec = jobLauncher.run(learningAggregationJob, monthlyParams);
        assertThat(monthlyExec.getExitStatus()).as("MONTHLY aggregation should complete")
                .isEqualTo(ExitStatus.COMPLETED);
    }
}
