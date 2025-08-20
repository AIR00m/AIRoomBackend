package com.airoom.airoom.classroom.model.service;

import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;

    /**
     * 클래스룸 학생 전체 조회
     */
    @Transactional(readOnly = true)
    public List<ClassroomStudentResponse> getClassroomStudentAll(final Long classroomNo) {
        return classroomStudentRepository.findClassroomStudentsByClassroomNo(classroomNo);
    }
}
