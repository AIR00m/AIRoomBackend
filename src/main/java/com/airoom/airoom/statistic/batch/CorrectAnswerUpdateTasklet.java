package com.airoom.airoom.statistic.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CorrectAnswerUpdateTasklet implements Tasklet {

    private final JdbcTemplate jdbc;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        String sql = """
                UPDATE airoom.learning_log l
                JOIN airoom.created_exam_problem cep
                  ON l.cep_no = cep.cep_no
                JOIN airoom.exam_problem ep
                  ON cep.ep_no = ep.ep_no
                SET l.ll_is_correct = (l.selected_answer = ep.ep_answer)
                WHERE l.ll_type = 'EXAM'
                  AND l.cep_no IS NOT NULL
                  AND l.selected_answer IS NOT NULL
                  AND l.updated_at >= DATE_SUB(NOW(), INTERVAL 1 DAY);
                """;

        int updated = jdbc.update(sql);

        log.info("정답 여부 보정 완료: {}건 수정", updated);

        return RepeatStatus.FINISHED;
    }
}
