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
            SELECT NEW com.airoom.airoom.board.model.dto.assign.AssignListResponse(
                    ab.assignBoardNo,
                    ab.assignBoardTitle,
                    at.groupAssignType,
                    ab.assignStart,
                    ab.assignEnd,
                    h.homeworkSubmitType
                    )
            FROM AssignTarget at
            JOIN Homework h ON h.assignTarget.assignTargetNo = at.assignTargetNo
            JOIN h.classroom c
            JOIN at.assignBoard ab
            WHERE at.targetNo = :classroomStudentNo AND h.classroom.classroomNo = :classroomNo
        """
    )
    List<AssignListResponse> findAssignTargetByClassroomNoAndClassroomStudentNo(@Param("classroomNo") Long classroomNo,
                                                                                @Param("classroomStudentNo") Long classroomStudentNo);

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
       WHERE ab.assignBoardNo = :assignBoardNo
""")
    List<StudentHomeworkResponse> findHomeworkListByAssignBoardNo (Long assignBoardNo, BoardType boardType);

    List<AssignListResponse> makeAssignListByAssignBoardNo (Long assignBoardNo);
}
