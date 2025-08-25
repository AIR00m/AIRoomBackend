package com.airoom.airoom.statistic.controller;

import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryRequest;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryResponse;
import com.airoom.airoom.statistic.model.service.StatisticService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticController implements StatisticControllerSwagger {
    private final StatisticService statisticService;

    /**
     * 학생 페이지 나의 학습 요약
     */
    @Override
    @PostMapping("/student")
    public StudentLearningSummaryResponse getMyLearningSummaryForStudent(
            @RequestBody @Valid final StudentLearningSummaryRequest request
    ) {
        return statisticService.getMyLearningSummaryForStudent(request);
    }
}
