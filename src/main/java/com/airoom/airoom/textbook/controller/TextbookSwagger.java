package com.airoom.airoom.textbook.controller;

import com.airoom.airoom.textbook.model.dto.UnitPdfUrl;
import com.airoom.airoom.textbook.model.dto.UnitsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Textbook 관련 API", description = "Textbook관련 API")
public interface TextbookSwagger {

    @Operation(
            summary = "Unit 목록 조회 API",
            description = "Textbook의 Unit 목록을 조회합니다"
    )
    public List<UnitsResponse> getUnitsByTextbookNo(@PathVariable Long textbookNo);

    @Operation(
            summary = "Unit 조회 API",
            description = "선택한 Unit의 정보를 조회합니다"
    )
    public List<UnitPdfUrl> getUnitByUnitNo(@PathVariable Long unitNo);
}
