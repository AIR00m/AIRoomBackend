package com.airoom.airoom.textbook.controller;

import com.airoom.airoom.textbook.entity.Textbook;
import com.airoom.airoom.textbook.model.service.TextbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/textbooks")
public class TextbookController {

    private final TextbookService textbookService;


}