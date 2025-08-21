package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import com.airoom.airoom.board.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    boolean existsByAssignTarget_AssignBoardAndAssignTarget_TargetNo(AssignBoard assignBoard, Long targetNo);
    boolean existsByAssignTarget(AssignTarget assignTarget);
}

