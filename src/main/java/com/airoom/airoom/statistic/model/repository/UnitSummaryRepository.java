package com.airoom.airoom.statistic.model.repository;

import com.airoom.airoom.statistic.entity.UnitSummary;
import com.airoom.airoom.statistic.entity.UnitSummaryId;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.dto.StudentUnitSummaryDetailResponse;
import com.airoom.airoom.statistic.model.dto.StudentUnitSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface UnitSummaryRepository extends JpaRepository<UnitSummary, UnitSummaryId> {
    @Query("""
                select new com.airoom.airoom.statistic.model.dto.StudentUnitSummaryResponse(
                    cast(coalesce( SUM(us.usTotalProblemsSolved),0) as long),
                    cast(coalesce( SUM(us.usTotalCorrectProblems),0) as long),
                    u.unitTitle,
                    u.unitNum
                )
                from UnitSummary us
                left join Unit u on us.id.usUnitNo = u.unitNo
                where us.id.usClassroomStudentNo in :studentNos
                and us.id.usType = :summaryType
                and us.id.usStartDate >= :usStartDate
                and us.usEndDate <= :usEndDate
                group by u.unitTitle, u.unitNum
            """)
    List<StudentUnitSummaryResponse> findByClassroomStudent(List<Long> studentNos, SummaryType summaryType, LocalDate usStartDate, LocalDate usEndDate);

    @Query("""
                select new com.airoom.airoom.statistic.model.dto.StudentUnitSummaryDetailResponse(
                    cs.student.memberName,
                    cast(coalesce( SUM(us.usTotalProblemsSolved),0) as long),
                    cast(coalesce( SUM(us.usTotalCorrectProblems),0) as long),
                    u.unitTitle,
                    u.unitNum
                )
                from UnitSummary us
                left join Unit u on u.unitNo = us.id.usUnitNo
                left join ClassroomStudent cs on us.id.usClassroomStudentNo = cs.classRoomStudentNo
                where us.id.usClassroomStudentNo in :studentNos
                  and us.id.usType = :summaryType
                  and us.id.usStartDate >= :usStartDate
                  and us.usEndDate <= :usEndDate
                group by cs.student.memberName, u.unitTitle, u.unitNum, us.id.usClassroomStudentNo
                order by us.id.usClassroomStudentNo, u.unitNum
            """)
    List<StudentUnitSummaryDetailResponse> findByClassroomStudentAndUnit(List<Long> studentNos, SummaryType summaryType, LocalDate usStartDate, LocalDate usEndDate);
}
