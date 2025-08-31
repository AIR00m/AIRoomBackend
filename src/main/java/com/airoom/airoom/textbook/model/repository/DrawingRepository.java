// src/main/java/com/airoom/airoom/textbook/model/repository/DrawingRepository.java
package com.airoom.airoom.textbook.model.repository;

import com.airoom.airoom.textbook.entity.Drawing;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DrawingRepository extends JpaRepository<Drawing, Long> {

    Optional<Drawing> findByUnit_UnitNoAndClassroomStudent_ClassRoomStudentNo(Long unitNo, Long studentNo);

    @Modifying
    @Query("""
        update Drawing d
           set d.drawingData = :data
         where d.unit.unitNo = :unitNo
           and d.classroomStudent.classRoomStudentNo = :studentNo
    """)
    int updateDataByUnitAndStudent(@Param("data") String data,
                                   @Param("unitNo") Long unitNo,
                                   @Param("studentNo") Long studentNo);
}
