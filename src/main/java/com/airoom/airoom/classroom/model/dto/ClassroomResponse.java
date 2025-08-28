package com.airoom.airoom.classroom.model.dto;

import com.airoom.airoom.common.value.Grade;
import com.airoom.airoom.common.value.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ClassroomResponse {
    private Long classroomNo;
    private String classroomSchool;
    private Grade classroomGrade;
    private String classroomClass;
    private Integer classroomYear;
    private Semester classroomSemester;
}
