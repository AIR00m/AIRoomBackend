package com.airoom.airoom.textbook.model.service;

import com.airoom.airoom.textbook.entity.Textbook;
import com.airoom.airoom.textbook.entity.Unit;
import com.airoom.airoom.textbook.model.dto.UnitsResponse;
import com.airoom.airoom.textbook.model.repository.TextbookRepository;
import com.airoom.airoom.textbook.model.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TextbookService {

    private final TextbookRepository textbookRepository;
    private final UnitRepository unitRepository;

    public List<Textbook> getAllTextbooks() {
        return textbookRepository.findAll();
    }
    public List<Textbook> getAllTextbooksByTeacherMemberNo(Long memberNo) {
        return textbookRepository.getAllTextbooksByTeacherMemberNo(memberNo);
    }
    public List<Textbook> getAllTextbooksByStudentMemberNo(Long memberNo) {
        return textbookRepository.getAllTextbooksByStudentMemberNo(memberNo);
    }
    public List<UnitsResponse> getUnitsByTextbookNo(Long textbookNo) {
        return unitRepository.findByTextbook_TextbookNo(textbookNo);
    }
}