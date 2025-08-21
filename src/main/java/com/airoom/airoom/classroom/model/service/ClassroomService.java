package com.airoom.airoom.classroom.model.service;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomTeacherRepository;
import com.amazonaws.services.kms.model.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;

    /**
     * 클래스룸 학생 전체 조회
     */
    @Transactional(readOnly = true)
    public List<ClassroomStudentResponse> getClassroomStudentAll(final Long classroomNo) {
        return classroomStudentRepository.findClassroomStudentsByClassroomNo(classroomNo);
    }

    public Long getClassroomNoByTeacherId(String memberId,Long textbookNo) {
        return classroomRepository.getClassroomNoByTextbookNoAndTeacherId(textbookNo,memberId)
                .orElseThrow(()-> new NotFoundException("해당하는 클래스룸 번호가 존재하지 않습니다."));
    }

    public Long getClassroomNoByStudentId(String memberId,Long textbookNo) {
        return classroomRepository.getClassroomNoByTextbookNoAndStudentId(textbookNo,memberId)
                .orElseThrow(()-> new NotFoundException("해당하는 클래스룸 번호가 존재하지 않습니다."));
    }

    public Long getClassTeacherNoByClassRoomNo(Long classroomNo){
        return classroomTeacherRepository.getClassTeacherNoByClassRoomNo(classroomNo);
    }

    public Long getClassStudentNoByClassRoomNoAndId(Long classroomNo, String memberId){
        return classroomStudentRepository.getClassStudentNoByClassRoomNo(classroomNo,memberId);
    }
}
