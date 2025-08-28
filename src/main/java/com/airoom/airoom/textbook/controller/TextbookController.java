package com.airoom.airoom.textbook.controller;

import com.airoom.airoom.exam.model.dto.UnitResponse;
import com.airoom.airoom.textbook.model.dto.UnitPdfUrl;
import com.airoom.airoom.textbook.model.dto.UnitsResponse;
import com.airoom.airoom.textbook.model.service.TextbookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}