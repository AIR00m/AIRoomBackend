package com.airoom.airoom.statistic.controller;

import com.airoom.airoom.statistic.model.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Statistic API", description = "통계/분석 관련 API")
public interface StatisticControllerSwagger {
    @Operation(
            summary = "학생 페이지 나의 학습요약 조회 API",
            description = "학생 페이지에서 나의 학습 요약을 조회합니다."
    )
    public StudentLearningSummaryResponse getMyLearningSummaryForStudent(
            @RequestBody @Valid StudentLearningSummaryRequest request
    );

    @Operation(
            summary = "학생 페이지 단원별 성취 현황 조회 API",
            description = "학생 페이지에서 단원별 성취 현황을 조회합니다."
    )
    public List<StudentUnitSummaryResponse> getUnitSummaryForStudent(
            @RequestBody @Valid final StudentLearningSummaryRequest request
    );

    @Operation(
            summary = "교사 페이지 우리반 학습요약 조회 API",
            description = "교사 페이지에서 우리반 학습 요약을 조회합니다."
    )
    public StudentLearningSummaryResponse getMyClassroomLearningSummary(
            @RequestBody @Valid ClassroomLearningSummaryRequest request
    );

    @Operation(
            summary = "교사 페이지 우리반 단원별 성취 현황 조회 API",
            description = "교사 페이지에서 우리반 단원별 성취 현황을 조회합니다."
    )
    public List<StudentUnitSummaryResponse> getMyClassroomUnitSummary(
            @RequestBody @Valid ClassroomLearningSummaryRequest request
    );

    @Operation(
            summary = "교사 페이지 우리반 단원별 상세 현황 조회 API",
            description = "교사 페이지에서 우리반 단원별 상세 현황을 조회합니다."
    )
    public List<StudentUnitSummaryDetailResponse> getMyClassroomUnitSummaryDetail(
            @RequestBody @Valid ClassroomLearningSummaryRequest request
    );

    @Operation(
            summary = "교사 페이지 우리반 학습 현황 관리 API",
            description = "교사 페이지에서 우리반 학습 현황 관리를 조회합니다."
    )
    public List<ClassroomLearningSummaryAllResponse> getMyClassroomLearningSummaryAll(
            @RequestBody @Valid ClassroomLearningSummaryRequest request
    );
}
