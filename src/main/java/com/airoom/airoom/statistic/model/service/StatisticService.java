package com.airoom.airoom.statistic.model.service;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.dto.ClassroomLearningSummaryRequest;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryRequest;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryResponse;
import com.airoom.airoom.statistic.model.dto.StudentUnitSummaryResponse;
import com.airoom.airoom.statistic.model.repository.LearningSummaryRepository;
import com.airoom.airoom.statistic.model.repository.UnitSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticService {
    private final LearningSummaryRepository learningSummaryRepository;
    private final UnitSummaryRepository unitSummaryRepository;
    private final ClassroomRepository classroomRepository;

    /**
     * 학생 페이지 나의 학습요약
     * SummaryType이 정해진 단위(MONTHLY, DAILY)인 경우 => 캐시 정보로 가져오기
     * 오늘 일자가 포함된 경우 ES에서 데이터를 가져와서 데이터 최신화해주기
     */
    public StudentLearningSummaryResponse getMyLearningSummaryForStudent(StudentLearningSummaryRequest request) {
        Result date = validateDate(request.lsStartDate(), request.lsEndDate(), request.lsType());

        StudentLearningSummaryResponse studentLearningSummaryResponse = getStudentLearningSummaryWithoutToday(List.of(request.classroomStudentNo()), request.lsType(), date.lsStartDate, date.lsEndDate);
        studentLearningSummaryResponse.calcAvgAccuracyRate();

        return studentLearningSummaryResponse;
    }

    /**
     * 학생 페이지 단원별 성취 현황
     */
    public List<StudentUnitSummaryResponse> getUnitSummaryForStudent(final StudentLearningSummaryRequest request) {
        Result date = validateDate(request.lsStartDate(), request.lsEndDate(), request.lsType());

        List<StudentUnitSummaryResponse> studentUnitSummaryResponseList = getUnitSummaryWithoutToday(List.of(request.classroomStudentNo()), request.lsType(), date.lsStartDate, date.lsEndDate);
        calcAvgAccuracyRate(studentUnitSummaryResponseList);

        return studentUnitSummaryResponseList;
    }

    /**
     * 교사용 클래스룸 학습 요약기능
     */
    public StudentLearningSummaryResponse getMyClassroomLearningSummary(final ClassroomLearningSummaryRequest request) {
        Result date = validateDate(request.lsStartDate(), request.lsEndDate(), request.lsType());

        Classroom classroom = loadClassroomFetchWithClassroomStudents(request);
        List<Long> studentNos = convertClassroomToStudentNos(classroom);

        StudentLearningSummaryResponse studentLearningSummaryResponse = getStudentLearningSummaryWithoutToday(studentNos, request.lsType(), date.lsStartDate, date.lsEndDate);
        studentLearningSummaryResponse.calcAvgAccuracyRate();

        return studentLearningSummaryResponse;
    }

    /**
     * 교사 페이지 우리반 단원별 성취 현황
     */
    public List<StudentUnitSummaryResponse> getMyClassroomUnitSummary(final ClassroomLearningSummaryRequest request) {
        Result date = validateDate(request.lsStartDate(), request.lsEndDate(), request.lsType());

        Classroom classroom = loadClassroomFetchWithClassroomStudents(request);
        List<Long> studentNos = convertClassroomToStudentNos(classroom);

        List<StudentUnitSummaryResponse> studentUnitSummaryResponseList = getUnitSummaryWithoutToday(studentNos, request.lsType(), date.lsStartDate, date.lsEndDate);
        calcAvgAccuracyRate(studentUnitSummaryResponseList);

        return studentUnitSummaryResponseList;
    }


    /**
     * 메소드 추출
     */
    private void calcAvgAccuracyRate(List<StudentUnitSummaryResponse> studentUnitSummaryResponseList) {
        for (StudentUnitSummaryResponse studentUnitSummaryResponse : studentUnitSummaryResponseList) {
            studentUnitSummaryResponse.setLsAvgAccuracyRate(
                    BigDecimal.valueOf(studentUnitSummaryResponse.getLsTotalCorrectProblems() / (double) studentUnitSummaryResponse.getLsTotalProblemsSolved())
                            .setScale(2, RoundingMode.HALF_UP));
        }
    }

    private List<Long> convertClassroomToStudentNos(Classroom classroom) {
        return classroom.getClassroomStudentList()
                .stream()
                .map(ClassroomStudent::getClassRoomStudentNo)
                .toList();
    }

    private List<StudentUnitSummaryResponse> getUnitSummaryWithoutToday(List<Long> studentNos, SummaryType lsType, LocalDate lsStartDate, LocalDate lsEndDate) {
        return unitSummaryRepository.findByClassroomStudent(studentNos, lsType, lsStartDate, lsEndDate);
    }

    private Result validateDate(LocalDate lsStartDate, LocalDate lsEndDate, SummaryType lsType) {
        if (lsEndDate == null) {
            lsEndDate = lsStartDate;
        }

        if (lsType == SummaryType.MONTHLY) {
            YearMonth ym = YearMonth.from(lsStartDate);
            lsStartDate = ym.atDay(1);
            lsEndDate = ym.atEndOfMonth();
        }
        return new Result(lsStartDate, lsEndDate);
    }

    private StudentLearningSummaryResponse getStudentLearningSummaryWithoutToday(List<Long> studentNos, SummaryType lsType, LocalDate lsStartDate, LocalDate lsEndDate) {
        return learningSummaryRepository.findByClassroomStudentAndTypeAndRange(studentNos, lsType, lsStartDate, lsEndDate);
    }

    private record Result(LocalDate lsStartDate, LocalDate lsEndDate) {

    }

    private Classroom loadClassroomFetchWithClassroomStudents(ClassroomLearningSummaryRequest request) {
        return classroomRepository.findClassroomByClassroomNoWithClassroomStudents(request.classroomNo());
    }
}
