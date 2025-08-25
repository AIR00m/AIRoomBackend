package com.airoom.airoom.textbook.model.repository;

import com.airoom.airoom.textbook.entity.Unit;
import com.airoom.airoom.textbook.model.dto.UnitPdfUrl;
import com.airoom.airoom.textbook.model.dto.UnitsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<UnitsResponse> findByTextbook_TextbookNo(Long textbookTextbookNo);

    @Query("""
           select new com.airoom.airoom.textbook.model.dto.UnitPdfUrl(
                    u.unitNo,
                    u.unitPdfUrl,
                    u.unitTitle
           )
           from Unit u
           where u.unitNo = :unitNo
           """)
    List<UnitPdfUrl> findUnitPdfUrlByUnitNo(Long unitNo);
}
