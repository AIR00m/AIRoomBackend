package com.airoom.airoom.chat.model.repository;

import com.airoom.airoom.chat.entity.ChatRoom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByClassroomTeacherAndClassroomStudent(ClassroomTeacher teacher, ClassroomStudent student);

    List<ChatRoom> findByClassroomTeacher(ClassroomTeacher teacher);

    @Query("select r.crNo from ChatRoom r where r.classroomTeacher.classroomTeacherNo = :teacherNo")
    List<Long> findIdsByTeacher(@Param("teacherNo") Long teacherNo);

    @Query("select r.crNo from ChatRoom r where r.classroomStudent.classRoomStudentNo = :studentNo")
    List<Long> findIdsByStudent(@Param("studentNo") Long studentNo);
}
