package com.airoom.airoom.board.controller;

import com.airoom.airoom.board.model.dto.homework.TeacherHomeworkRequest;
import com.airoom.airoom.board.model.service.HomeworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/homework")
@RequiredArgsConstructor
@Slf4j

public class HomeworkController implements HomeworkControllerSwagger {

    private final HomeworkService homeworkService;
    // 선생님이 학생 점수 등록
    @PostMapping("/teacher/assign/{boardNo}")
    public ResponseEntity<Void> saveStudentHomeworkScores
            (@PathVariable Long boardNo, @RequestBody List<TeacherHomeworkRequest> request){
      int saveResult =  homeworkService.saveStudentHomeworkScore(boardNo,request);
        return ResponseEntity.ok().build();
    }
}
