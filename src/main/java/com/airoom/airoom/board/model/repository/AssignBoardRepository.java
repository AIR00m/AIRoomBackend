package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.AssignBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignBoardRepository extends JpaRepository<AssignBoard, Long> {

    List<AssignBoard> findAssignBoardByClassroomClassroomNo(Long classroomNo);
}

