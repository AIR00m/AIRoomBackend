package com.airoom.airoom.classroom.model.service;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomTeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return classroomRepository.getClassroomNoByTextbookNoAndTeacherId(textbookNo,memberId);
    }

    public Long getClassroomNoByStudentId(String memberId,Long textbookNo) {
        return classroomRepository.getClassroomNoByTextbookNoAndStudentId(textbookNo,memberId);
    }

    public Long getClassTeacherNoByClassRoomNo(Long classroomNo){
        return classroomTeacherRepository.getClassTeacherNoByClassRoomNo(classroomNo);
    }

    public Long getClassStudentNoByClassRoomNo(Long classroomNo){
        return classroomStudentRepository.getClassStudentNoByClassRoomNo(classroomNo);
    }
}
