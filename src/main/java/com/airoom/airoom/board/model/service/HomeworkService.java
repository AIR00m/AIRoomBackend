package com.airoom.airoom.board.model.service;

import com.airoom.airoom.board.entity.Homework;
import com.airoom.airoom.board.model.dto.homework.TeacherHomeworkRequest;
import com.airoom.airoom.board.model.repository.HomeworkRepository;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomTeacherRepository;
import com.airoom.airoom.common.redis.RedisStreamPublisher;
import com.airoom.airoom.notification.entity.value.NotificationType;
import com.airoom.airoom.notification.model.dto.NotificationEventDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeworkService {
    private final HomeworkRepository homeworkRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final ClassroomRepository classroomRepository;
    private final RedisStreamPublisher publisher;


    /**
     * 선생님 점수를 입력시 채점 점수 등록 메소드
     * @param boardNo 게시판 번호
     * @param request 숙제 요청
     * @return 숫자
     */
    @Transactional
    public int saveStudentHomeworkScore(Long boardNo, List<TeacherHomeworkRequest> request) {
        List<Homework> homeworkList
                = homeworkRepository.findHomeworkByAssignBoardNo(boardNo);

        Map<Long, Integer> scores = request.stream()
                .collect(Collectors.toMap(TeacherHomeworkRequest::memberNo, TeacherHomeworkRequest::homeworkScore));

        int updatedCount = 0;

        List<Long> scoredStudentMemberNos = new ArrayList<>();

        for (Homework homework : homeworkList) {
            Long memberNo = homework.getMember().getMemberNo();
            if (scores.containsKey(memberNo)) {
                homework.updateScore(scores.get(memberNo));
                scoredStudentMemberNos.add(memberNo);
                updatedCount++;
            }
        }

        if (!scoredStudentMemberNos.isEmpty()) {
            sendHomeworkScoreNotification(scoredStudentMemberNos);
        }

        return updatedCount;
    }

    private void sendHomeworkScoreNotification(List<Long> scoredStudentMemberNos) {

        NotificationEventDto notificationEventDto = new NotificationEventDto(
                NotificationType.ASSIGNMENT_GRADED.getLocation(),
                NotificationType.ASSIGNMENT_GRADED,
                scoredStudentMemberNos  // 여러 학생들에게 동시 발송
        );
        publisher.publishNotification(notificationEventDto);

    }

    @Transactional
    public void saveStudentHomework(Long homeworkBoardNo, String content) {
        if (content == null) {
            content = "";
        }
        Homework homework = homeworkRepository.findById(homeworkBoardNo)
                .orElseThrow(() -> new EntityNotFoundException("Homework not found: " + homeworkBoardNo));

        int updated = homeworkRepository.updateContent(homeworkBoardNo, content);

        Long classroomNo = classroomRepository.getClassroomNoByHomeworkBoardNo(homeworkBoardNo);

        Long classroomTeacherNo = classroomTeacherRepository.getClassTeacherNoByClassRoomNo(classroomNo);

        sendHomeworkNotification(classroomTeacherNo);
    }

    private void sendHomeworkNotification(Long classroomTeacherNo) {

        Long targetMemberNo = classroomTeacherRepository.getMemberNoByClassroomTeacherNo(classroomTeacherNo);

        NotificationEventDto notificationEventDto = new NotificationEventDto(
                NotificationType.ASSIGNMENT_SUBMITTED.getLocation(), NotificationType.ASSIGNMENT_SUBMITTED,List.of(targetMemberNo)
        );
        publisher.publishNotification(notificationEventDto);

    }
}



