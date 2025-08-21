package com.airoom.airoom.textbook.controller;

import com.airoom.airoom.textbook.model.dto.UnitsResponse;
import com.airoom.airoom.textbook.model.service.TextbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/textbooks")
public class TextbookController {

    private final TextbookService textbookService;

    /* 단원 목록 조회 */
    @GetMapping("/units/{textbookNo}")
    public List<UnitsResponse> getUnitsByTextbookNo(@PathVariable Long textbookNo) {
        return textbookService.getUnitsByTextbookNo(textbookNo);
    }

    /* 단원 조회 */

}