package com.airoom.airoom.board.model.service;

import com.airoom.airoom.board.entity.Homework;
import com.airoom.airoom.board.model.dto.homework.TeacherHomeworkRequest;
import com.airoom.airoom.board.model.repository.HomeworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeworkService {
    private final HomeworkRepository homeworkRepository;

    @Transactional
    public int saveStudentHomeworkScore(Long boardNo, List<TeacherHomeworkRequest> request){
        List<Homework> homeworkList
                = homeworkRepository.findHomeworkByAssignBoardNo(boardNo);
        for(Homework h : homeworkList){

        }
        return 0;
    }


}
