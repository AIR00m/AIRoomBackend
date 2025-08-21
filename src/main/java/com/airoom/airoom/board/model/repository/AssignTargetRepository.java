package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignTargetRepository extends JpaRepository<AssignTarget, Long> {

    boolean existsByAssignBoardAndGroupAssignTypeTrue(AssignBoard assignBoard);

    @Query("""
                    select at
                    from AssignTarget at
                    join fetch at.assignBoard ab
                    join fetch ab.classroom cr
                    where cr.classroomNo = :classroomNo and at.groupAssignType=false and at.targetNo = :targetNo
            """)
    List<AssignTarget> findByAssignBoard_Classroom_ClassroomNoAndTargetNoAndGroupAssignTypeFalse(Long classroomNo, Long targetNo);

    List<AssignTarget> findByAssignBoard_Classroom_ClassroomNoAndTargetNoAndGroupAssignTypeTrue(Long classroomNo, Long targetNo);
}
