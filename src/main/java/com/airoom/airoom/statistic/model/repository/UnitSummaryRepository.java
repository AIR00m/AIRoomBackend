package com.airoom.airoom.statistic.model.repository;

import com.airoom.airoom.statistic.entity.UnitSummary;
import com.airoom.airoom.statistic.entity.UnitSummaryId;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.dto.StudentUnitSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
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
                and us.id.usClassroomStudentNo in :studentNos
                and us.id.usType = :summaryType
                and us.id.usStartDate >= :usStartDate
                and us.usEndDate <= :usEndDate
                group by u.unitTitle, u.unitNum
            """)
    List<StudentUnitSummaryResponse> findByClassroomStudent(List<Long> studentNos, SummaryType summaryType, LocalDate usStartDate, LocalDate usEndDate);
}
