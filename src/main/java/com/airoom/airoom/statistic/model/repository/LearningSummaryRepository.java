package com.airoom.airoom.statistic.model.repository;

import com.airoom.airoom.statistic.entity.LearningSummary;
import com.airoom.airoom.statistic.entity.LearningSummaryId;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface LearningSummaryRepository extends JpaRepository<LearningSummary, LearningSummaryId> {
    @Query("""
            SELECT MAX(ls.createdAt)
            FROM LearningSummary ls
            WHERE ls.id.lsClassroomStudentNo = :studentNo
            AND ls.id.lsType = :summaryType
            """)
    LocalDateTime findLastBatchCreatedAt(Long studentNo, SummaryType summaryType); //마지막 배치처리 시간조회

    @Query("""
            select new com.airoom.airoom.statistic.model.dto.StudentLearningSummaryResponse(
                SUM(ls.lsTotalLearningDays),
                SUM(ls.lsTotalLearningTimeMs),
                SUM(ls.lsTotalProblemsSolved),
                SUM(ls.lsTotalCorrectProblems)
            )
            from LearningSummary ls
            where ls.id.lsClassroomStudentNo = :classroomStudent
            and ls.id.lsType = :summaryType
            and ls.id.lsStartDate >= :lsStartDate
            and ls.lsEndDate <= :lsEndDate
            """)
    StudentLearningSummaryResponse findByClassroomStudentAndTypeAndRange(Long classroomStudent, SummaryType summaryType, LocalDate lsStartDate, LocalDate lsEndDate);
}
