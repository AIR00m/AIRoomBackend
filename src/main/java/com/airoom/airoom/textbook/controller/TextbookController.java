package com.airoom.airoom.textbook.controller;

import com.airoom.airoom.exam.model.dto.UnitResponse;
import com.airoom.airoom.textbook.model.dto.*;
import com.airoom.airoom.textbook.model.service.TextbookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/textbooks")
public class TextbookController implements TextbookSwagger{

    private final TextbookService textbookService;

    /* 단원 목록 조회 */
    @GetMapping("/units/{textbookNo}")
    public List<UnitsResponse> getUnitsByTextbookNo(@PathVariable Long textbookNo) {
        return textbookService.getUnitsByTextbookNo(textbookNo);
    }

    /* 단원 조회 */
    @GetMapping("/units/pdf/{unitNo}")
    public List<UnitPdfUrl> getUnitByUnitNo(@PathVariable("unitNo") Long unitNo) {
        log.info(">>>>> PDF URL 요청 수신 - unitNo: {}", unitNo);
        return textbookService.getUnitByUnitNo(unitNo);
    }

    /* 단원 진도 저장 */
    @PutMapping("/progress/lastpage")
    public ResponseEntity<Void> saveProgress(@RequestBody UpdateProgress request) {
        textbookService.saveProgress(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/drawings/save")
    public ResponseEntity<Void> save(@RequestBody SaveDrawingRequest req) {
        textbookService.saveOrUpdate(req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/drawings/load/{classRoomStudentNo}/{unitNo}")
    public ResponseEntity<DrawingResponse> load(
            @PathVariable Long classRoomStudentNo,
            @PathVariable Long unitNo
    ) {
        return ResponseEntity.ok(textbookService.load(classRoomStudentNo, unitNo));
    }
}