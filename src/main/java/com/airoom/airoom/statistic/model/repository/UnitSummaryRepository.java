package com.airoom.airoom.statistic.model.repository;

import com.airoom.airoom.statistic.entity.UnitSummary;
import com.airoom.airoom.statistic.entity.UnitSummaryId;
import com.airoom.airoom.statistic.model.dto.StudentUnitSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UnitSummaryRepository extends JpaRepository<UnitSummary, UnitSummaryId> {
    @Query("""
                select new com.airoom.airoom.statistic.model.dto.StudentUnitSummaryResponse(
                    SUM(us.usTotalProblemsSolved),
                    SUM(us.usTotalCorrectProblems),
                    u.unitTitle,
                    u.unitNum
                )
                from UnitSummary us, Unit u
                where u.unitNo = us.id.usUnitNo
                and us.id.usClassroomStudentNo = :classroomStudentNo
                group by u.unitTitle, u.unitNum
            """)
    List<StudentUnitSummaryResponse> findByClassroomStudent(Long classroomStudentNo);
}
