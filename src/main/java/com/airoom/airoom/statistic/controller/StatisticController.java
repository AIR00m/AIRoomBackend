package com.airoom.airoom.statistic.controller;

import com.airoom.airoom.statistic.model.dto.*;
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
    @PostMapping("/student/summary")
    public StudentLearningSummaryResponse getMyLearningSummaryForStudent(
            @RequestBody @Valid final StudentLearningSummaryRequest request
    ) {
        return statisticService.getMyLearningSummaryForStudent(request);
    }


    /**
     * 학생 페이지 단원별 성취 현황
     */
    @Override
    @PostMapping("/student/unit-summary")
    public List<StudentUnitSummaryResponse> getUnitSummaryForStudent(
            @RequestBody @Valid final StudentLearningSummaryRequest request
    ) {
        return statisticService.getUnitSummaryForStudent(request);
    }


    /**
     * 교사 페이지 우리반 학습 요약
     */
    @Override
    @PostMapping("/teacher/summary")
    public StudentLearningSummaryResponse getMyClassroomLearningSummary(
            @RequestBody @Valid final ClassroomLearningSummaryRequest request
    ) {
        return statisticService.getMyClassroomLearningSummary(request);
    }

    /**
     * 교사 페이지 우리반 단원별 성취 현황
     */
    @Override
    @PostMapping("/teacher/unit-summary")
    public List<StudentUnitSummaryResponse> getMyClassroomUnitSummary(
            @RequestBody @Valid final ClassroomLearningSummaryRequest request) {
        return statisticService.getMyClassroomUnitSummary(request);
    }

    /**
     * 교사 페이지 우리반 단원별 상세 현황
     */
    @Override
    @PostMapping("/teacher/unit-summary/detail")
    public List<StudentUnitSummaryDetailResponse> getMyClassroomUnitSummaryDetail(
            @RequestBody @Valid final ClassroomLearningSummaryRequest request
    ) {
        return statisticService.getMyClassroomUnitSummaryDetail(request);
    }
}
