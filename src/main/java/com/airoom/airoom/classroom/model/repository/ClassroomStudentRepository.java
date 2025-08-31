package com.airoom.airoom.classroom.model.repository;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.model.dto.ClassroomResponse;
import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomStudentRepository extends JpaRepository<ClassroomStudent, Long> {
    @Query("""
                select new com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse(
                                cs.classRoomStudentNo, st.memberName, st.memberId
                )
                from ClassroomStudent cs
                join cs.student st
                where cs.classRoom.classroomNo = :classroomNo
            """)
    List<ClassroomStudentResponse> findClassroomStudentsByClassroomNo(@Param("classroomNo") Long classroomNo);

    @Query("SELECT cs.classRoomStudentNo FROM ClassroomStudent cs WHERE cs.student.memberNo = :memberNo")
    List<Long> getClassroomStudentNosByMemberNo(@Param("memberNo") Long memberNo);

    @Query("""
                SELECT cs.classRoomStudentNo
                FROM ClassroomStudent cs
                JOIN cs.classRoom c
                JOIN cs.student s
                WHERE c.classroomNo = :classroomNo and s.memberId= :memberId
           """)
    Long getClassStudentNoByClassRoomNo(@Param("classroomNo")  Long classroomNo, @Param("memberId") String memberId);

    Optional<ClassroomStudent> findTopByStudent_MemberNoOrderByCreatedAtDesc(Long memberNo);

    @Query("""
        select distinct s.memberNo
        from ClassroomStudent cs
        join cs.student s
        where cs.classRoom.classroomNo = :classroomNo
    """)
    List<Long> findTargetMemberNosByClassroomNo(@Param("classroomNo") Long classroomNo);

    @Query("""
        select distinct s.memberNo
        from ClassroomStudent cs
        join cs.student s
        where cs.classRoomStudentNo in :ids
    """)
    List<Long> findMemberNosByClassroomStudentNos(@Param("ids") List<Long> ids);
}

