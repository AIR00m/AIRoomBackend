package com.airoom.airoom.classroom.model.repository;

import com.airoom.airoom.classroom.entity.ClassroomGroup;
import com.airoom.airoom.classroom.model.dto.ClassroomGroupResponse;
import com.airoom.airoom.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomGroupRepository extends JpaRepository<ClassroomGroup, Long> {

    // JPQL으로 바로 DTO 조회
    @Query("""
            SELECT new com.airoom.airoom.classroom.model.dto.ClassroomGroupResponse(
                        cg.groupNo, cg.groupName)
            FROM ClassroomGroup cg
            WHERE cg.classroom.classroomNo = :classroomNo
            """)
    //AND cg.deletedAt IS NULL
    List<ClassroomGroupResponse> findClassroomGroupsByClassroomNo(@Param("classroomNo") Long classroomNo);
}