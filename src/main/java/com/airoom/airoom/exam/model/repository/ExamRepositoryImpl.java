package com.airoom.airoom.exam.model.repository;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.common.value.MemberRole;
import com.airoom.airoom.exam.entity.QExam;
import com.airoom.airoom.exam.entity.QExamUnit;
import com.airoom.airoom.exam.entity.QStudentExam;
import com.airoom.airoom.exam.entity.value.ExamStatus;
import com.airoom.airoom.exam.model.dto.ExamListResponse;
import com.airoom.airoom.exam.model.dto.UnitResponse;
import com.airoom.airoom.textbook.entity.QUnit;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.querydsl.core.types.dsl.Expressions.constant;

@RequiredArgsConstructor
public class ExamRepositoryImpl implements ExamRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ExamListResponse> getExamsByClassroomAndExamStatusAndMemberRole(
            Classroom classroom, Long classroomMemberNo, ExamStatus examStatus, MemberRole memberRole
    ) {
        QExam exam = QExam.exam;
        QStudentExam studentExam = QStudentExam.studentExam;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(exam.classroom.eq(classroom));

        LocalDateTime now = LocalDateTime.now();

        // examStatus 필터링 조건
        if (examStatus != ExamStatus.ALL) {
            if (memberRole == MemberRole.STUDENT) {
                if (examStatus == ExamStatus.COMPLETE) {
                    builder.and(
                            exam.examEndTime.before(now)
                                    .or(studentExam.seIsDone.isTrue())
                    );
                } else {
                    builder.and(
                            exam.examEndTime.after(now)
                                    .and(studentExam.seIsDone.isFalse())
                    );
                }
            } else {
                if (examStatus == ExamStatus.COMPLETE) {
                    builder.and(exam.examEndTime.before(now));
                } else {
                    builder.and(exam.examEndTime.after(now));
                }
            }
        }

        // 동적으로 ExamStatus 계산
        Expression<String> statusCase = memberRole == MemberRole.STUDENT
                ? new CaseBuilder()
                .when(exam.examEndTime.before(now).or(studentExam.seIsDone.isTrue()))
                .then(constant(ExamStatus.COMPLETE.getLabel()))
                .otherwise(constant(ExamStatus.INCOMPLETE.getLabel()))
                : new CaseBuilder()
                .when(exam.examEndTime.before(now))
                .then(constant(ExamStatus.COMPLETE.getLabel()))
                .otherwise(constant(ExamStatus.INCOMPLETE.getLabel()));

        // 단원 목록 미리 조회 N+1 방지
        QExamUnit examUnit = QExamUnit.examUnit;
        QUnit unit = QUnit.unit;

        List<UnitResponse> unitResponseList = queryFactory
                .select(Projections.constructor(UnitResponse.class,
                        unit.unitNo,
                        unit.unitNum,
                        unit.unitTitle,
                        examUnit.exam.examNo
                ))
                .from(examUnit)
                .join(examUnit.unit, unit)
                .fetch();

        // 시험 번호별 단원 리스트 매핑
        Map<Long, List<UnitResponse>> unitMap = unitResponseList.stream()
                .collect(Collectors.groupingBy(UnitResponse::examNo));

        // 메인 시험 목록 조회
        List<ExamListResponse> examList = queryFactory
                .select(Projections.constructor(ExamListResponse.class,
                        exam.examNo,
                        exam.examName,
                        statusCase,
                        exam.examProblemCount,

                        // 총 응시자 수
                        JPAExpressions.select(studentExam.count())
                                .from(studentExam)
                                .where(studentExam.exam.eq(exam)),

                        // 응시 완료자 수
                        JPAExpressions.select(studentExam.count())
                                .from(studentExam)
                                .where(studentExam.exam.eq(exam)
                                        .and(studentExam.seIsDone.isTrue())),

                        // 평균 점수
                        JPAExpressions.select(studentExam.seScore.avg().round().intValue())
                                .from(studentExam)
                                .where(studentExam.exam.eq(exam)),
                        exam.examStartTime,
                        exam.examEndTime,
                        studentExam.seIsDone
                ))
                .from(exam)
                .leftJoin(studentExam).on(studentExam.exam.eq(exam)
                        .and(studentExam.classroomStudent.classRoomStudentNo.eq(classroomMemberNo)))
                .where(builder)
                .fetch();

        // 단원 리스트 추가 주입
        for (ExamListResponse response : examList) {
            List<UnitResponse> units = unitMap.getOrDefault(response.getExamNo(), List.of());
            response.setUnitResponseList(units);
        }

        return examList;
    }
}
