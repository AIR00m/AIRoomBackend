package com.airoom.airoom.classroom.model.service;

import com.airoom.airoom.classroom.model.dto.ClassroomGroupResponse;
import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.repository.ClassroomGroupRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomTeacherRepository;
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
    private final ClassroomGroupRepository classroomGroupRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;

    /**
     * 클래스룸 학생 전체 조회
     */
    @Transactional(readOnly = true)
    public List<ClassroomStudentResponse> getClassroomStudentAll(final Long classroomNo) {
        return classroomStudentRepository.findClassroomStudentsByClassroomNo(classroomNo);
    }
    @Transactional(readOnly = true)
    public List<ClassroomGroupResponse> getClassroomGroupAll(final Long classroomNo) {
        return classroomGroupRepository.findClassroomGroupsByClassroomNo(classroomNo);
    }

    public Classroom getClassroom(Long classroomNo) {
        return classroomRepository.findById(classroomNo).orElse(null);
    }

    // 년도가 바뀜에 따라 한명이 여러 클래스룸을 가질 수 있으므로 List
    public List<Long> getClassroomStudentNosByMemberNo(Long memberNo) {
        return classroomStudentRepository.getClassroomStudentNosByMemberNo(memberNo);
    }

    public List<Long> getClassroomTeacherNosByMemberNo(Long memberNo) {
        return classroomTeacherRepository.getClassroomTeacherNosByMemberNo(memberNo);
    }
}
