package com.airoom.airoom.exam.model.service;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.exam.entity.*;
import com.airoom.airoom.exam.entity.value.ProblemLevel;
import com.airoom.airoom.exam.model.dto.*;
import com.airoom.airoom.exam.model.repository.ExamProblemRepository;
import com.airoom.airoom.exam.model.repository.ExamRepository;
import com.airoom.airoom.exam.model.repository.StudentExamRepository;
import com.airoom.airoom.textbook.entity.Unit;
import com.airoom.airoom.textbook.model.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final ExamProblemRepository examProblemRepository;
    private final ClassroomRepository classroomRepository;
    private final UnitRepository unitRepository;
    private final StudentExamRepository studentExamRepository;

    /**
     * 시험 생성
     * 시험단원(EXAM_UNIT), 시험(EXAM), 시험출제문제(CREATED_EXAM_PROBLEM), 학생시험(STUDENT_EXAM) 트랜잭션으로 묶기
     */
    public Long createExam(final CreateExamRequest request) {
        Classroom classroom = loadClassroom(request.classroomNo());
        Exam exam = buildExam(request, classroom);

        //단원 추가
        addUnitToExam(request.unitNoList(), exam);
        //시험문제 추가
        addExamProblemToExam(request.epNoList(), exam);
        //시험대상 학생추가
        addClassroomStudentToExam(request.classroomStudentNoList(), classroom, exam);

        Exam savedExam = examRepository.save(exam);
        return savedExam.getExamNo();
    }

    private void addClassroomStudentToExam(List<Long> classroomStudentNoList, Classroom classroom, Exam exam) {
        //요청받은 학생고유번호
        HashSet<Long> targetStudentIds = new HashSet<>(classroomStudentNoList);
        
        //실제 Classroom 학생 고유번호
        Set<Long> actualStudentIds = classroom.getClassroomStudentList().stream()
                .map(ClassroomStudent::getClassRoomStudentNo)
                .collect(Collectors.toSet());

        //요청값 중 유효하지 않은 학생검증
        List<Long> invalidIds = targetStudentIds.stream().filter(id -> !actualStudentIds.contains(id)).toList();
        if (!invalidIds.isEmpty()) {
            throw new IllegalArgumentException("클래스룸 소속이 아닌 학생이 포함되어 있습니다. : " + invalidIds);
        }

        //StudentExam 생성 및 Exam 추가
        classroom.getClassroomStudentList().stream()
                .filter(cs -> targetStudentIds.contains(cs.getClassRoomStudentNo()))
                .map(cs -> StudentExam.builder()
                        .seIsDone(false)
                        .classroomStudent(cs)
                        .build())
                .forEach(exam::addStudentExam);
    }

    /**
     * 난이도, 단원별 랜덤 문제 출제
     */
    @Transactional(readOnly = true)
    public CreateExamProblemsResponse getExamProblemsByLevelAndUnit(final CreateExamProblemsRequest request) {
        List<ExamProblemResponse> examProblemResponseList = new ArrayList<>();
        for (ExamProblemRequest examProblemRequest : request.examProblemRequestList()) {
            Long unitNo = examProblemRequest.unitNo();
            addRandomProblemsByUnitAndLevelWithCount(examProblemRequest, examProblemResponseList, unitNo);
        }
        return new CreateExamProblemsResponse(examProblemResponseList);
    }

    private void addRandomProblemsByUnitAndLevelWithCount(ExamProblemRequest examProblemRequest, List<ExamProblemResponse> examProblemResponseList, Long unitNo) {
        for (Map.Entry<ProblemLevel, Integer> entry : examProblemRequest.problemCountsByLevel().entrySet()) {
            ProblemLevel problemLevel = entry.getKey();
            Integer count = entry.getValue();
            examProblemResponseList.addAll(examProblemRepository.findRandomExamProblemByUnitAndLevel(unitNo, problemLevel, PageRequest.of(0, count)));
        }
    }

    private void addExamProblemToExam(final List<Long> epNoList, final Exam exam) {
        List<ExamProblem> examProblemList = examProblemRepository.findAllById(epNoList);
        Map<Long, ExamProblem> examProblemMap = examProblemList.stream().collect(Collectors.toMap(ExamProblem::getEpNo, e -> e));
        int order = 0;
        for (Long epNo : epNoList) {
            ExamProblem ep = examProblemMap.get(epNo);
            if (ep == null) {
                throw new IllegalArgumentException("존재하지 않는 시험문제 번호입니다. : " + epNo);
            }
            CreatedExamProblem cep = CreatedExamProblem.builder()
                    .cepQuestionOrder(++order)
                    .examProblem(ep)
                    .build();

            exam.addCreatedExamProblem(cep);
        }
    }

    private void addUnitToExam(final List<Long> unitNoList, final Exam exam) {
        List<Unit> unitList = unitRepository.findAllById(unitNoList);
        //N+1 문제 방지 => Map 사용하지 않고 findById로 조회하면 N+1문제 발생, 요청과 실제 조회된 데이터 매칭을 위해서 Map 사용
        Map<Long, Unit> unitMap = unitList.stream().collect(Collectors.toMap(Unit::getUnitNo, u -> u));
        for (Long unitNo : unitNoList) {
            Unit unit = unitMap.get(unitNo);
            if (unit == null) {
                throw new IllegalArgumentException("존재하지 않는 단원번호입니다. : " + unitNo);
            }
            ExamUnit examUnit = ExamUnit.builder()
                    .unit(unit)
                    .build();

            exam.addExamUnit(examUnit);
        }
    }

    private Exam buildExam(final CreateExamRequest request, final Classroom classroom) {
        return Exam.builder()
                .examName(request.examName())
                .examStartTime(request.examStartTime())
                .examEndTime(request.examEndTime())
                .examProblemCount(request.examProblemCount())
                .classroom(classroom)
                .build();
    }

    private Classroom loadClassroom(final Long classroomNo) {
        return classroomRepository.findByIdWithStudents(classroomNo).orElseThrow(
                () -> new IllegalArgumentException("올바르지 않은 클래스룸 고유번호입니다. 다시 확인하세요 : " + classroomNo)
        );
    }
}
