package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import com.airoom.airoom.board.entity.BoardType;
import com.airoom.airoom.board.model.dto.assign.AssignListResponse;
import com.airoom.airoom.board.model.dto.homework.StudentHomeworkResponse;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignTargetRepository extends JpaRepository<AssignTarget, Long> {

    boolean existsByAssignBoardAndGroupAssignTypeTrue(AssignBoard assignBoard);
    @Query(
            """
            SELECT new com.airoom.airoom.board.model.dto.assign.AssignListResponse(
                ab.assignBoardNo,
                ab.assignBoardTitle,
                at.groupAssignType,
                ab.assignStart,
                ab.assignEnd,
                h.homeworkSubmitType
            )
            FROM Homework h
            JOIN h.assignTarget at
            JOIN at.assignBoard ab
            JOIN h.classroom c
            WHERE c.classroomNo = :classroomNo
              And at.groupAssignType = false
              AND at.targetNo = :classroomStudentNo
        """
    )
    List<AssignListResponse> findAssignTargetByClassroomNoAndClassroomStudentNo(Long classroomNo, Long classroomStudentNo);
    @Query(
            """
        SELECT new com.airoom.airoom.board.model.dto.assign.AssignListResponse(
                ab.assignBoardNo,
                ab.assignBoardTitle,
                at.groupAssignType,
                ab.assignStart,
                ab.assignEnd,
                h.homeworkSubmitType
            )
            FROM Homework h
            JOIN h.assignTarget at
            JOIN at.assignBoard ab
            JOIN h.classroom c
            WHERE c.classroomNo = :classroomNo
                AND at.groupAssignType = true
              AND at.targetNo = :targetNo
    """
    )
    List<AssignListResponse> findByAssignBoardTypeGroupByClassroomNoAndGroupNo(Long classroomNo, Long targetNo);

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
       WHERE ab.assignBoardNo = :assignBoardNo
""")
    List<StudentHomeworkResponse> findHomeworkListByAssignBoardNo (Long assignBoardNo, BoardType boardType);

    @Query(
            """
            SELECT at
            FROM AssignTarget at
            WHERE at.assignBoard.assignBoardNo = :assignBoardNo
        """
    )
    AssignTarget findAssignTargetByAssignBoardNo(Long assignBoardNo);
}
