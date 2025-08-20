package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignTargetRepository extends JpaRepository<AssignTarget, Long> {

    boolean existsByAssignBoardAndGroupAssignTypeTrue(AssignBoard assignBoard);
}
