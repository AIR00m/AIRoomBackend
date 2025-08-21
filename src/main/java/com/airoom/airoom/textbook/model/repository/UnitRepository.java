package com.airoom.airoom.textbook.model.repository;

import com.airoom.airoom.textbook.entity.Unit;
import com.airoom.airoom.textbook.model.dto.UnitsResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<UnitsResponse> findByTextbook_TextbookNo(Long textbookTextbookNo);
}
