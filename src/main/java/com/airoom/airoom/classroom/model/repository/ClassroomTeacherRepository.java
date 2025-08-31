package com.airoom.airoom.classroom.model.repository;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ClassroomTeacherRepository extends CrudRepository<ClassroomTeacher,Long> {
    @Query
            ("""
                SELECT ct.classroomTeacherNo
                FROM ClassroomTeacher ct
                JOIN ct.classroom c
                WHERE c.classroomNo = :classroomNo
           """)
    Long getClassTeacherNoByClassRoomNo (@Param("classroomNo") Long classroomNo);

    List<ClassroomTeacher> classroomTeacherNo(Long classroomTeacherNo);

    @Query("""
        select t.teacher.memberNo
        from Homework h
        join h.classroom c
        join ClassroomTeacher t on t.classroom = c
        where h.homeworkBoardNo = :homeworkBoardNo
    """)
    Long getTeacherMemberNoByHomeworkBoardNo(@Param("homeworkBoardNo") Long homeworkBoardNo);
}
