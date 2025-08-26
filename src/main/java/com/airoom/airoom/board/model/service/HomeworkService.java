package com.airoom.airoom.board.model.service;

import com.airoom.airoom.board.entity.Homework;
import com.airoom.airoom.board.model.dto.homework.TeacherHomeworkRequest;
import com.airoom.airoom.board.model.repository.HomeworkRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeworkService {
    private final HomeworkRepository homeworkRepository;


    // 선생님 점수를 입력시 채점 점수 등록 메소드
    @Transactional
    public int saveStudentHomeworkScore(Long boardNo, List<TeacherHomeworkRequest> request) {
        List<Homework> homeworkList
                = homeworkRepository.findHomeworkByAssignBoardNo(boardNo);

        Map<Long, Integer> scores = request.stream()
                .collect(Collectors.toMap(TeacherHomeworkRequest::memberNo, TeacherHomeworkRequest::homeworkScore));

        int updatedCount = 0;

        for (Homework homework : homeworkList) {
            Long memberNo = homework.getMember().getMemberNo();
            if (scores.containsKey(memberNo)) {
                homework.updateScore(scores.get(memberNo));
                updatedCount++;
            }
        }
        return updatedCount;
    }

    public void saveStudentHomework(Long homeworkBoardNo, String content) {
        if (content == null) content = "";
        Homework homework = homeworkRepository.findById(homeworkBoardNo)
                .orElseThrow(() -> new EntityNotFoundException("Homework not found: " + homeworkBoardNo));
        int updated = homeworkRepository.updateContent(homeworkBoardNo, (content == null ? "" : content.trim()));

    }
}



