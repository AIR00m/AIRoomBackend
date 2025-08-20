package com.airoom.airoom.classroom.model.repository;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface ClassroomTeacherRepository extends CrudRepository<ClassroomTeacher,Long> {
    @Query
            ("""
                SELECT ct.classroomTeacherNo
                FROM ClassroomTeacher ct
                JOIN ct.classroom c
                WHERE c.classroomNo = :classroomNo
           """)
    Long getClassTeacherNoByClassRoomNo (@Param("classroomNo") Long classroomNo);

}
