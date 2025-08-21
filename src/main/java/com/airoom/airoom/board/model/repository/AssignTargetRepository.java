package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignTargetRepository extends JpaRepository<AssignTarget, Long> {

    boolean existsByAssignBoardAndGroupAssignTypeTrue(AssignBoard assignBoard);

    List<AssignTarget> findByAssignBoard_Classroom_ClassroomNoAndTargetNoAndGroupAssignTypeFalse(Long classroomNo, Long targetNo);

    List<AssignTarget> findByAssignBoard_Classroom_ClassroomNoAndTargetNoAndGroupAssignTypeTrue(Long classroomNo, Long targetNo);
}
