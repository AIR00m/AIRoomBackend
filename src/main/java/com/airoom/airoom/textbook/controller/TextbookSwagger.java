package com.airoom.airoom.textbook.controller;

import com.airoom.airoom.textbook.model.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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

    @Operation(
            summary = "Unit 저장 API",
            description = "단원의 진도를 저장합니다"
    )
    public ResponseEntity<Void> saveProgress(@RequestBody UpdateProgress request);

    @Operation(
            summary = "그림 저장 API",
            description = "해당교재의 학생그림을 저장합니다"
    )
    public ResponseEntity<Void> save(@RequestBody SaveDrawingRequest req);

    @Operation(
            summary = "그림 조회 API",
            description = "해당교재의 학생그림를 조회합니다"
    )
    public ResponseEntity<DrawingResponse> load(
            @PathVariable Long classRoomStudentNo,
            @PathVariable Long unitNo
    );
}
