package com.airoom.airoom.classroom.controller;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.model.dto.ClassroomGroupResponse;
import com.airoom.airoom.classroom.model.dto.ClassroomResponse;
import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/classroom")
public class ClassroomController implements ClassroomControllerSwagger {
    private final ClassroomService classroomService;

    /**
     * 클래스룸 학생 전체 조회
     */
    @Override
    @GetMapping("/student/{classroomNo}")
    public List<ClassroomStudentResponse> getClassroomStudentAll(@PathVariable Long classroomNo) {
        return classroomService.getClassroomStudentAll(classroomNo);
    }
    /**
     * 클래스룸 그룹 전체 조회
     */
    @Override
    @GetMapping("/group/{classroomNo}")
    public List<ClassroomGroupResponse> getAssignmentGroup(@PathVariable Long classroomNo) {
        return classroomService.getClassroomGroupAll(classroomNo);
    }
    @GetMapping("/{classroomNo}")
    public ClassroomResponse getClassroom(@PathVariable Long classroomNo) {
        return classroomService.getClassroomByClassroomNo(classroomNo);
    }

}
