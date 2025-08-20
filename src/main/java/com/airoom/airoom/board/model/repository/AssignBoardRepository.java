package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignBoardRepository extends JpaRepository<AssignBoard, Long> {
    @Query("""
        SELECT DISTINCT ab FROM AssignBoard ab
        JOIN AssignTarget at ON ab.assignBoardNo = at.assignBoard.assignBoardNo
        JOIN ClassroomStudent cs ON at.targetNo = cs.classRoomStudentNo
        WHERE ab.classroom.classroomNo = :classroomNo
        AND cs.student.memberNo = :studentMemberNo
    """)
    List<AssignBoard> findAssignmentsForStudent(@Param("classroomNo") Long classroomNo, @Param("studentMemberNo") Long studentMemberNo);

    List<AssignBoard> findByClassroomClassroomNo(Long classroomNo);
}

