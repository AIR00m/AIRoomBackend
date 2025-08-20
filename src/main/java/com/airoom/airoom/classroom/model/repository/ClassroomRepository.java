package com.airoom.airoom.classroom.model.repository;

import com.airoom.airoom.classroom.entity.Classroom;
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
                    SELECT ct.classroom
                    FROM ClassroomTeacher ct
                    JOIN ct.teacher m
                    JOIN ct.textbook t
                    WHERE m.memberId = :memberId AND t.textbookNo = :textbookNo
            """)
    Long getClassroomNoByTextbookNoAndTeacherId(@Param("textbookNo") Long textbookNo,@Param("memberId") String memberId);
    @Query(
            """
                 select distinct c.classroomNo
                    from ClassroomTeacher ct
                      join ct.classroom c
                      join c.classroomStudentList cs
                      join cs.student s
                    where s.memberId = :memberId
                      and ct.textbook.textbookNo = :textbookNo
            """
    )
    Long getClassroomNoByTextbookNoAndStudentId(@Param("textbookNo") Long textbookNo,@Param("memberId") String memberId);
}
