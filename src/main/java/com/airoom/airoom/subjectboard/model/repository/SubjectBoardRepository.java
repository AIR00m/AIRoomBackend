package com.airoom.airoom.subjectboard.model.repository;

import com.airoom.airoom.board.entity.SubjectBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectBoardRepository extends JpaRepository<SubjectBoard,Long> {
}
