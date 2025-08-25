package com.airoom.airoom.textbook.model.repository;

import com.airoom.airoom.textbook.entity.Unit;
import com.airoom.airoom.textbook.model.dto.UnitsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<UnitsResponse> findByTextbook_TextbookNo(Long textbookTextbookNo);
}
