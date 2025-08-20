package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {

    @Query("""
        SELECT COUNT(h) > 0 FROM Homework h
        JOIN h.assignTarget at
        JOIN ClassroomStudent cs ON at.targetNo = cs.classRoomStudentNo
        WHERE at.assignBoard = :assignBoard
        AND cs.student.memberNo = :memberNo
    """)
    boolean existsByAssignBoardAndStudentMemberNo(@Param("assignBoard") AssignBoard assignBoard,
                                                  @Param("memberNo") Long memberNo);
}

