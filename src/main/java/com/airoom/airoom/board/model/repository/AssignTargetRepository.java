package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import com.airoom.airoom.board.entity.BoardType;
import com.airoom.airoom.board.model.dto.homework.StudentHomeworkResponse;
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

    @Query("""
      SELECT
        NEW com.airoom.airoom.board.model.dto.homework.StudentHomeworkResponse(
        m.memberNo,
        m.memberName,
        h.homeworkSubmitType,
        h.createdAt,
        h.updatedAt,
        a.originalName,
        a.s3Key,
        h.homeworkBoardContent,
        h.homeworkScore)
      FROM Homework h
      JOIN h.assignTarget at
      JOIN at.assignBoard ab
      JOIN h.member m
      LEFT JOIN Attachment a
        ON a.boardNo = h.homeworkBoardNo
       AND a.boardType = :boardType
""")
    List<StudentHomeworkResponse> findHomeworkListByAssignBoardNo (Long assignBoardNo, BoardType boardType);
}
