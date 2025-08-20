package com.airoom.airoom.classroom.model.repository;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassroomTeacherRepository extends CrudRepository<ClassroomTeacher,Long> {
    @Query("SELECT cs.classroomTeacherNo FROM ClassroomTeacher cs WHERE cs.teacher.memberNo = :memberNo")
    List<Long> getClassroomTeacherNosByMemberNo(@Param("memberNo") Long memberNo);
}
