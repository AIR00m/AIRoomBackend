package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.model.dto.assign.AssignResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignBoardRepository extends JpaRepository<AssignBoard, Long> {

    List<AssignBoard> findAssignBoardByClassroomClassroomNo(Long classroomNo);

    @Query
            ("""
                SELECT  new com.airoom.airoom.board.model.dto.assign.AssignResponse (
                ab.assignBoardNo,
                ab.assignBoardTitle,
                ab.assignStart,
                ab.assignEnd,
                ab.assignBoardContent,
                at.groupAssignType,
                h.homeworkBoardNo,
                h.homeworkBoardContent
                )
                FROM AssignBoard ab
                JOIN AssignTarget at ON at.assignBoard.assignBoardNo = ab.assignBoardNo
                JOIN Homework h ON h.assignTarget.assignTargetNo = at.assignTargetNo
                WHERE ab.assignBoardNo = :assignBoardNo And at.targetNo = :classroomStudentNo
            """
            )
    AssignResponse getAssignBoardByBoardNo(Long assignBoardNo, Long classroomStudentNo);

}
