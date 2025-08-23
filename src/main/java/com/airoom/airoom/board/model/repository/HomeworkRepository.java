package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.BoardType;
import com.airoom.airoom.board.entity.AssignBoard;
import com.airoom.airoom.board.entity.AssignTarget;
import com.airoom.airoom.board.entity.Homework;
import com.airoom.airoom.board.model.dto.homework.HomeworkRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    boolean existsByAssignTarget(AssignTarget assignTarget);
   
    @Query("""
    SELECT h
    FROM Homework h
    JOIN h.assignTarget at
    JOIN at.assignBoard ab
    WHERE at.groupAssignType = false
     AND ab.assignBoardNo = :assignBoardNo
        
    """)
    List<Homework> findHomeworkByAssignBoardNo(Long assignBoardNo);


}

