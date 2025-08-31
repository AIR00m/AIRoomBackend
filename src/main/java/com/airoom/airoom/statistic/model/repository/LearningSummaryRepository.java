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
                    cs.classRoomStudentNo,
                    cs.student.memberName,
            
                    (select coalesce(sum(ls.lsTotalLearningTimeMs),0)
                     from LearningSummary ls
                     where ls.id.lsClassroomStudentNo = cs.classRoomStudentNo),
            
                    (select coalesce(sum(ls.lsTotalProblemsSolved),0)
                     from LearningSummary ls
                     where ls.id.lsClassroomStudentNo = cs.classRoomStudentNo),
            
                    (select coalesce(sum(ls.lsTotalCorrectProblems),0)
                     from LearningSummary ls
                     where ls.id.lsClassroomStudentNo = cs.classRoomStudentNo),
            
                    (select coalesce(sum(p.progressLastPage),0)
                     from Progress p
                     where p.classroomStudent.classRoomStudentNo = cs.classRoomStudentNo),
            
                    (select coalesce(sum(h.homeworkScore),0)
                     from Homework h
                     where h.assignTarget.targetNo = cs.classRoomStudentNo
                       and h.assignTarget.groupAssignType = false),
            
                    (select coalesce(count(h.homeworkScore),0)
                     from Homework h
                     where h.assignTarget.targetNo = cs.classRoomStudentNo
                       and h.assignTarget.groupAssignType = false)
                )
                from ClassroomStudent cs
                where cs.classRoomStudentNo in :studentNos
                order by cs.student.memberName
            """)
    List<ClassroomLearningSummaryAllResponse> findByClassroomStudentAll(List<Long> studentNos);


    Optional<LearningSummary> findTopById_LsClassroomStudentNoOrderByCreatedAtDesc(Long csNo);
}
