package com.airoom.airoom.subjectboard.model.repository;

import com.airoom.airoom.board.entity.SubjectBoard;
import com.airoom.airoom.classroom.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SubjectBoardRepository extends JpaRepository<SubjectBoard,Long> {
    List<SubjectBoard> findByClassroom(Classroom classroom);
}
