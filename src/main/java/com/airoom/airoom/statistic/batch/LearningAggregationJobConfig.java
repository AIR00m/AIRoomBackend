package com.airoom.airoom.statistic.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class LearningAggregationJobConfig {
    public static final String JOB_NAME = "learningAggregationJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private static final String STEP_SUMMARY = "aggregateLearningSummaryStep";
    private static final String STEP_UNIT    = "aggregateUnitSummaryStep";

    @Bean(name = JOB_NAME)
    public Job learningAggregationJob(Step aggregateLearningSummaryStep,
                                      Step aggregateUnitSummaryStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(aggregateLearningSummaryStep)
                .next(aggregateUnitSummaryStep)
                .build();
    }

    @Bean
    public Step aggregateLearningSummaryStep(LearningSummaryAggregationTasklet tasklet) {
        return new StepBuilder(STEP_SUMMARY, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step aggregateUnitSummaryStep(UnitSummaryAggregationTasklet tasklet) {
        return new StepBuilder(STEP_UNIT, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
