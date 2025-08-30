package com.airoom.airoom.textbook.model.service;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.textbook.entity.Progress;
import com.airoom.airoom.textbook.entity.Textbook;
import com.airoom.airoom.textbook.entity.Unit;
import com.airoom.airoom.textbook.model.dto.UnitPdfUrl;
import com.airoom.airoom.textbook.model.dto.UnitsResponse;
import com.airoom.airoom.textbook.model.dto.UpdateProgress;
import com.airoom.airoom.textbook.model.repository.ProgressRepository;
import com.airoom.airoom.textbook.model.repository.TextbookRepository;
import com.airoom.airoom.textbook.model.repository.UnitRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//@Transactional
@RequiredArgsConstructor
public class TextbookService {

    @PersistenceContext
    private EntityManager em;

    private final TextbookRepository textbookRepository;
    private final UnitRepository unitRepository;
    private final ProgressRepository progressRepository;


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

    public List<UnitPdfUrl> getUnitByUnitNo(Long unitNo) {
        return unitRepository.findUnitPdfUrlByUnitNo(unitNo);
    }

    @Transactional
    public void saveProgress(UpdateProgress req) {
        if (req == null || req.classRoomStudentNo() == null
                || req.unitNo() == null || req.progressLastPage() == null) return;

        ClassroomStudent csRef = em.getReference(ClassroomStudent.class, req.classRoomStudentNo());
        Unit unitRef           = em.getReference(Unit.class, req.unitNo());

        boolean exists = progressRepository
                .findByClassroomStudentAndUnit(csRef, unitRef)
                .isPresent();

        if (exists) {
            // 더 클 때만 업데이트
            progressRepository.updateLastPageIfGreater(csRef, unitRef, req.progressLastPage());
        } else {
            // 없으면 INSERT
            Progress p = Progress.builder()
                    .classroomStudent(csRef)
                    .unit(unitRef)
                    .progressLastPage(req.progressLastPage())
                    .build();
            progressRepository.save(p);
        }
    }
}