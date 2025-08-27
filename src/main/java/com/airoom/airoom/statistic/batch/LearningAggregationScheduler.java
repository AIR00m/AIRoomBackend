package com.airoom.airoom.statistic.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class LearningAggregationScheduler {
    private final JobLauncher jobLauncher;
    private final Job learningAggregationJob; //이전에 만든 Job(요약 Step 2개 연결)

    //매일 오전 3시 이전날 데이터 배치처리를 통한 집계
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void runDaily() throws Exception {
        LocalDate target = LocalDate.now().minusDays(1); //어제 날짜 데이터 저장

        JobParameters params = new JobParametersBuilder()
                .addString("summaryType", "DAILY")
                .addString("targetDate", target.toString())
                .addLong("ts", System.currentTimeMillis(), false) //서로 다른 JobParameters를 만들어서 Spring Batch가 다른 파라미터로 인식하게 만드려는 의도 (PK 역할)
                .toJobParameters();

        jobLauncher.run(learningAggregationJob, params);
    }

    //매월 1일 4시 이전달 데이터 배치처리를 통한 집계
    @Scheduled(cron = "0 0 4 1 * *", zone = "Asia/Seoul")
    public void runMonthly() throws Exception {
        LocalDate lastMonthAnyDay = LocalDate.now().minusMonths(1).withDayOfMonth(15);

        JobParameters params = new JobParametersBuilder()
                .addString("summaryType", "MONTHLY")
                .addString("targetDate", lastMonthAnyDay.toString())
                .addLong("ts", System.currentTimeMillis(), false)
                .toJobParameters();

        jobLauncher.run(learningAggregationJob, params);
    }
}
