package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.common.value.MemberRole;
import com.airoom.airoom.exam.entity.value.ExamStatus;
import com.airoom.airoom.exam.model.dto.ExamListResponse;

import java.util.List;

public interface ExamRepositoryCustom {
    List<ExamListResponse> getExamsByClassroomAndExamStatusAndMemberRole(Classroom classroom, Long classroomMemberNo, ExamStatus examStatus, MemberRole memberRole);
}
