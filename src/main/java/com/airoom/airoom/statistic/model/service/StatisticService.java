package com.airoom.airoom.statistic.model.service;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.statistic.entity.value.SummaryType;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryRequest;
import com.airoom.airoom.statistic.model.dto.StudentLearningSummaryResponse;
import com.airoom.airoom.statistic.model.repository.LearningSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticService {
    private final LearningSummaryRepository learningSummaryRepository;
    private final ClassroomStudentRepository classroomStudentRepository;

    /**
     * 학생 페이지 나의 학습요약
     * SummaryType이 정해진 단위(MONTHLY, DAILY)인 경우 => 캐시 정보로 가져오기
     * 오늘 일자가 포함된 경우 ES에서 데이터를 가져와서 데이터 최신화해주기
     */
    public StudentLearningSummaryResponse getMyLearningSummaryForStudent(StudentLearningSummaryRequest request) {
        LocalDateTime today = LocalDateTime.now();
        LocalDate lsStartDate = request.lsStartDate();
        LocalDate lsEndDate = request.lsEndDate();

        if (lsEndDate == null) {
            lsEndDate = lsStartDate;
        }

        if (request.lsType() == SummaryType.MONTHLY) {
            YearMonth ym = YearMonth.from(lsStartDate);
            lsStartDate = ym.atDay(1);
            lsEndDate = ym.atEndOfMonth();
        }

        //RDB에 저장된 데이터 가져오기
        StudentLearningSummaryResponse studentLearningSummaryResponse = learningSummaryRepository.findByClassroomStudentAndTypeAndRange(request.classroomStudentNo(), request.lsType(), lsStartDate, lsEndDate);

        //EndDate가 오늘 날짜라면 ES에서 최신 데이터 가져와서 보정
        if (!lsEndDate.isBefore(ChronoLocalDate.from(today))) {
            //마지막 배치시간 이후의 데이터를 ES에서 가져오기
            LocalDateTime lastBatchTime = learningSummaryRepository.findLastBatchCreatedAt(request.classroomStudentNo(), request.lsType());

            //추후 logstash.conf 완성되면 ES 데이터까지 합쳐서 최신 데이터로 보정하기
        }

        studentLearningSummaryResponse.calc(0L, 0L, 0L, 0L);
        return studentLearningSummaryResponse;
    }

    /**
     * 메소드 추출
     */
    private ClassroomStudent loadClassroomStudent(Long classroomStudentNo) {
        return classroomStudentRepository.findById(classroomStudentNo).orElseThrow(
                () -> new IllegalArgumentException("잘못된 클래스룸학생 고유번호입니다. : " + classroomStudentNo)
        );
    }
}
