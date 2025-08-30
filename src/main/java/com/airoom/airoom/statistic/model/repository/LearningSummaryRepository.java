package com.airoom.airoom.statistic.model.repository;

import com.airoom.airoom.statistic.entity.LearningSummary;
import com.airoom.airoom.statistic.entity.LearningSummaryId;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.dto.ClassroomLearningSummaryAllResponse;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
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
                cast(coalesce(SUM(ls.lsTotalLearningDays),0)as long),
                cast(coalesce(SUM(ls.lsTotalLearningTimeMs),0)as long),
                cast(coalesce(SUM(ls.lsTotalProblemsSolved),0)as long),
                cast(coalesce(SUM(ls.lsTotalCorrectProblems),0)as long)
            )
            from LearningSummary ls
            where ls.id.lsClassroomStudentNo in :studentNos
            and ls.id.lsType = :summaryType
            and ls.id.lsStartDate >= :lsStartDate
            and ls.lsEndDate <= :lsEndDate
            """)
    StudentLearningSummaryResponse findByClassroomStudentAndTypeAndRange(List<Long> studentNos, SummaryType summaryType, LocalDate lsStartDate, LocalDate lsEndDate);

    @Query("""
                select new com.airoom.airoom.statistic.model.dto.ClassroomLearningSummaryAllResponse(
                        ls.id.lsClassroomStudentNo,
                        cs.student.memberName,
                        cast(coalesce(SUM(ls.lsTotalLearningTimeMs),0) as long),
                        cast(coalesce(SUM(ls.lsTotalProblemsSolved),0) as long),
                        cast(coalesce(SUM(ls.lsTotalCorrectProblems),0) as long),
                        cast(coalesce(SUM(p.progressLastPage),0) as long),
                        cast(coalesce(SUM(h.homeworkScore),0) as long),
                        cast(coalesce(COUNT(h.homeworkScore),0) as long)
                    )
                    from LearningSummary ls
                    left join ClassroomStudent cs
                           on cs.classRoomStudentNo = ls.id.lsClassroomStudentNo
                    left join Homework h
                           on h.assignTarget.targetNo = cs.classRoomStudentNo
                          and h.assignTarget.groupAssignType = false
                    left join Progress p
                           on p.classroomStudent.classRoomStudentNo = ls.id.lsClassroomStudentNo
                    left join Unit u
                           on u = p.unit
                    where ls.id.lsClassroomStudentNo in :studentNos
                    group by cs.classRoomStudentNo, cs.student.memberName
                    order by cs.student.memberName
            """)
    List<ClassroomLearningSummaryAllResponse> findByClassroomStudentAll(List<Long> studentNos);

    Optional<LearningSummary> findTopById_LsClassroomStudentNoOrderByCreatedAtDesc(Long csNo);
}
