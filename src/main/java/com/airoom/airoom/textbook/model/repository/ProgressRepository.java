package com.airoom.airoom.textbook.model.repository;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.textbook.entity.Progress;
import com.airoom.airoom.textbook.entity.Unit;
import com.airoom.airoom.textbook.model.dto.UnitProgressResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    @Query("""
           select p
           from Progress p
           where p.classroomStudent = :cs
             and p.unit = :unit
           """)
    Optional<Progress> findByClassroomStudentAndUnit(
            @Param("cs") ClassroomStudent cs,
            @Param("unit") Unit unit
    );

    @Modifying
    @Query("""
           update Progress p
              set p.progressLastPage = :lastPage
            where p.classroomStudent = :cs
              and p.unit = :unit
              and p.progressLastPage < :lastPage
           """)
    int updateLastPageIfGreater(@Param("cs") ClassroomStudent cs,
                                @Param("unit") Unit unit,
                                @Param("lastPage") Integer lastPage);

    @Query("""
    SELECT new com.airoom.airoom.textbook.model.dto.UnitProgressResponseDto(
        u.unitNo, 
        u.unitTitle, 
        u.unitNum,
        p.progressLastPage,
        p.updatedAt
    )
    FROM Progress p 
    JOIN Unit u ON p.unit.unitNo = u.unitNo 
    WHERE p.classroomStudent.classRoomStudentNo = :classroomStudentNo 
    ORDER BY p.updatedAt DESC 
    LIMIT 1
    """)
    Optional<UnitProgressResponseDto> findLatestProgressByClassroomStudentNo(@Param("classroomStudentNo") Long classroomStudentNo);

}