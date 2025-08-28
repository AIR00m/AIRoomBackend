package com.airoom.airoom.classroom.model.repository;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.model.dto.ClassroomResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("""
                    select c
                    from Classroom c
                    inner join fetch c.classroomStudentList s
                    where c.classroomNo = :id
            """)
    Optional<Classroom> findByIdWithStudents(@Param("id") Long id);

    @Query("""
                    SELECT c.classroomNo
                    FROM ClassroomTeacher ct
                    JOIN ct.teacher m
                    JOIN ct.textbook t
                    JOIN ct.classroom c
                    WHERE m.memberId = :memberId AND t.textbookNo = :textbookNo
            """)
    Optional<Long> getClassroomNoByTextbookNoAndTeacherId(@Param("textbookNo") Long textbookNo,@Param("memberId") String memberId);
    @Query(
            """
                 SELECT c.classroomNo
                 FROM ClassroomTeacher ct
                 JOIN ct.textbook tb
                 JOIN ct.classroom c
                 JOIN c.classroomStudentList cs
                 JOIN cs.student s
                 WHERE s.memberId = :memberId
                   AND tb.textbookNo = :textbookNo
            """
    )
    Optional<Long> getClassroomNoByTextbookNoAndStudentId(@Param("textbookNo") Long textbookNo, @Param("memberId") String memberId);


    @Query("""
            select distinct c
            from Classroom c
            left join fetch c.classroomStudentList st
            where c.classroomNo = :classroomNo
            """)
    Classroom findClassroomByClassroomNoWithClassroomStudents(Long classroomNo);

    @Query("""
           SELECT NEW com.airoom.airoom.classroom.model.dto.ClassroomResponse(
                      c.classroomNo,
                      c.classroomSchool,
                      c.classroomGrade,
                      c.classroomClass,
                      c.classroomYear,
                      c.classroomSemester
                      )
           FROM Classroom c
           where c.classroomNo = :classroomNo
                                                """)
    ClassroomResponse getClassroomByClassroomNo (Long classroomNo);
}
