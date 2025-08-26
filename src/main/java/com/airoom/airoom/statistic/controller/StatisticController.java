package com.airoom.airoom.statistic.controller;

import com.airoom.airoom.statistic.model.dto.StudentUnitSummaryResponse;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryRequest;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryResponse;
import com.airoom.airoom.statistic.model.service.StatisticService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    /**
     * 학생 페이지 단원별 성취 현황
     */
    @Override
    @GetMapping("/student/{classroomStudentNo}")
    public List<StudentUnitSummaryResponse> getLearningSummaryByUnit(@PathVariable Long classroomStudentNo) {
        return statisticService.getLearningSummaryByUnit(classroomStudentNo);
    }
}
