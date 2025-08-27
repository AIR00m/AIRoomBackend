package com.airoom.airoom.exam.model.service;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomTeacherRepository;
import com.airoom.airoom.common.value.MemberRole;
import com.airoom.airoom.exam.entity.*;
import com.airoom.airoom.exam.entity.value.ExamStatus;
import com.airoom.airoom.exam.entity.value.ProblemLevel;
import com.airoom.airoom.exam.model.dto.*;
import com.airoom.airoom.exam.model.repository.*;
import com.airoom.airoom.textbook.entity.Unit;
import com.airoom.airoom.textbook.model.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final CreatedExamProblemRepository createdExamProblemRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final StudentExamRepository studentExamRepository;

    /**
     * 시험 생성
     * 시험단원(EXAM_UNIT), 시험(EXAM), 시험출제문제(CREATED_EXAM_PROBLEM), 학생시험(STUDENT_EXAM) 트랜잭션으로 묶기
     */
    public Long createExam(final CreateExamRequest request) {
        Classroom classroom = loadClassroom(request.classroomNo());
        Exam exam = buildExam(request, classroom);

        addUnitToExam(request.unitNoList(), exam);
        addExamProblemToExam(request.epNoList(), exam);
        addClassroomStudentToExam(request.classroomStudentNoList(), classroom, exam);

        Exam savedExam = examRepository.save(exam);
        return savedExam.getExamNo();
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

    /**
     * 시험문제 교체
     */
    public ExamProblemResponse replaceExamProblem(final ReplaceExamProblemRequest request) {
        ExamProblem target = loadExamProblem(request.epNo());
        return getRandomExamProblemByUnitAndLevelExcludingSelf(target);
    }

    /**
     * 시험출제문제 전체조회 = 시험 상세조회
     */
    public ExamDetailResponse getExamProblems(final Long examNo) {
        List<ExamProblemDetailResponse> examProblemDetailResponseList = createdExamProblemRepository.findCreatedExamProblemsByExamNo(examNo);
        if (examProblemDetailResponseList == null || examProblemDetailResponseList.isEmpty()) {
            throw new IllegalArgumentException("잘못된 시험고유번호 입니다. : " + examNo);
        }
        return new ExamDetailResponse(examNo, examProblemDetailResponseList);
    }

    /**
     * 시험문제 채점 & 제출
     * 학생응답(STUDENT_ANSWER), 학생시험(STUDENT_EXAM) 트랜잭션으로 묶어서 진행
     */
    public SubmitExamProblemsResponse markAndSubmitExamProblems(final SubmitExamProblemsRequest request) {
        ClassroomStudent classroomStudent = loadClassroomStudent(request.classroomStudentNo());
        Exam exam = loadExam(request.examNo());

        //검증 로직
        final List<StudentAnswerRequest> studentAnswerRequests = validateStudentAnswerRequestList(request);

        final int problemCounts = request.studentAnswerRequestList().size();
        final double scorePerProblem = 100.0 / problemCounts;

        List<StudentAnswerResponse> studentAnswerResponseList = new ArrayList<>(problemCounts);
        List<StudentAnswer> studentAnswerList = new ArrayList<>(problemCounts);

        //채점 로직
        Result result = markProblems(studentAnswerRequests, scorePerProblem, studentAnswerResponseList, studentAnswerList, classroomStudent, exam);

        //영속성 저장 로직
        studentAnswerRepository.saveAll(studentAnswerList);

        StudentExam se = loadStudentExam(classroomStudent, exam);
        se.updateStudentExam(result.roundScore, request.seStartTime(), request.seEndTime());
        exam.addStudentExam(se);

        return new SubmitExamProblemsResponse(exam.getExamName(), result.totalSolvingTime(), request.seStartTime(), result.roundScore(), studentAnswerResponseList);
    }

    /**
     * 전체 시험 조회
     * 멤버 타입별로 교사, 학생별 데이터가 다름
     */
    @Transactional(readOnly = true)
    public List<ExamListResponse> getExams(final Long classroomMemberNo, final ExamStatus examStatus, final MemberRole memberRole) {
        Classroom classroom;
        classroom = loadClassroomByMemberRole(classroomMemberNo, memberRole);

        return examRepository.getExamsByClassroomAndExamStatusAndMemberRole(classroom, classroomMemberNo, examStatus, memberRole);
    }

    /**
     * 시험별 학생 정답 리스트 조회
     */
    @Transactional(readOnly = true)
    public List<StudentAnswerResponse> getStudentAnswersByClassroomStudent(final Long classroomStudentNo, final Long examNo) {
        ClassroomStudent classroomStudent = loadClassroomStudent(classroomStudentNo);
        Exam exam = loadExam(examNo);

        return studentAnswerRepository.findStudentAnswersByClassroomStudentAndExam(classroomStudent, exam);
    }

    /**
     * 시험별 학급 정답 리스트 조회
     */
    @Transactional(readOnly = true)
    public List<StudentAnswerByClassroomResponse> getStudentAnswersByClassroom(final Long classroomNo, final Long examNo) {
        Classroom classroom = loadClassroom(classroomNo);
        Exam exam = loadExam(examNo);

        List<StudentAnswer> studentAnswerList = studentAnswerRepository.findStudentAnswersByClassroomAndExam(classroom, exam);
        return convertStudentAnswerToDtoAndGrouping(studentAnswerList);
    }





    /**
     * 메소드 추출
     */
    private ExamProblemResponse getRandomExamProblemByUnitAndLevelExcludingSelf(ExamProblem examProblem) {
        ProblemLevel level = examProblem.getEpLevel();
        Unit unit = examProblem.getUnit();
        List<ExamProblemResponse> examProblemResponseList = examProblemRepository.findRandomByUnitAndLevelExcludingSelf(unit.getUnitNo(), level, examProblem.getEpNo(), PageRequest.of(0, 1));
        return examProblemResponseList.get(0);
    }

    private List<StudentAnswerByClassroomResponse> convertStudentAnswerToDtoAndGrouping(List<StudentAnswer> studentAnswerList) {
        return studentAnswerList.stream()
                .collect(Collectors.groupingBy(
                        StudentAnswer::getClassroomStudent,
                        Collectors.mapping(
                                sa -> new StudentAnswerResponse(
                                        sa.getExamProblem().getUnit().getUnitTitle(),
                                        sa.isSaIsCorrect(),
                                        sa.getCreatedExamProblem().getCepQuestionOrder(),
                                        sa.getCreatedExamProblem().getCepNo(),
                                        sa.getExamProblem().getEpNo(),
                                        sa.getSaSolvingTime(),
                                        sa.getSaAnswer(),
                                        sa.getExamProblem().getEpAnswer()
                                ),
                                Collectors.toList()
                        )
                )).entrySet().stream()
                .map(e -> new StudentAnswerByClassroomResponse(
                        e.getKey().getClassRoomStudentNo(),
                        e.getKey().getStudent().getMemberName(),
                        e.getValue()
                )).toList();
    }

    private Result markProblems(List<StudentAnswerRequest> studentAnswerRequests, double scorePerProblem, List<StudentAnswerResponse> studentAnswerResponseList, List<StudentAnswer> studentAnswerList, ClassroomStudent classroomStudent, Exam exam) {
        double totalScore = 0.0;
        Duration totalSolvingTime = Duration.ZERO;
        //N+1 문제 방지를 위해 미리 먼저 조회해오기
        Map<Long, ExamProblem> examProblemMap = loadExamProblems(studentAnswerRequests);
        Map<Long, CreatedExamProblem> createdExamProblemMap = loadCreatedExamProblems(studentAnswerRequests);

        for (StudentAnswerRequest studentAnswerRequest : studentAnswerRequests) {
            ExamProblem examProblem = examProblemMap.get(studentAnswerRequest.epNo());
            CreatedExamProblem createdExamProblem = createdExamProblemMap.get(studentAnswerRequest.cepNo());

            boolean isCorrect = examProblem.getEpAnswer().equals(studentAnswerRequest.saAnswer());
            if (isCorrect) {
                totalScore += scorePerProblem;
            }

            Duration solvingTime = studentAnswerRequest.saSolvingTime();
            totalSolvingTime = solvingTime != null ? totalSolvingTime.plus(solvingTime) : totalSolvingTime;

            studentAnswerResponseList.add(new StudentAnswerResponse(examProblem.getUnit().getUnitTitle(), isCorrect, createdExamProblem.getCepQuestionOrder(), createdExamProblem.getCepNo(), examProblem.getEpNo(), studentAnswerRequest.saSolvingTime(), studentAnswerRequest.saAnswer(), examProblem.getEpAnswer()));
            studentAnswerList.add(buildStudentAnswer(studentAnswerRequest, isCorrect, examProblem, classroomStudent, createdExamProblem, exam));
        }
        int roundScore = Math.min((int) Math.round(totalScore), 100);
        return new Result(totalSolvingTime, roundScore);
    }

    private List<StudentAnswerRequest> validateStudentAnswerRequestList(SubmitExamProblemsRequest request) {
        final List<StudentAnswerRequest> studentAnswerRequests = request.studentAnswerRequestList();
        if (studentAnswerRequests == null || studentAnswerRequests.isEmpty()) {
            throw new IllegalArgumentException("학생 응답이 비어있습니다.");
        }
        return studentAnswerRequests;
    }

    private StudentAnswer buildStudentAnswer(StudentAnswerRequest studentAnswerRequest, boolean isCorrect, ExamProblem examProblem, ClassroomStudent classroomStudent, CreatedExamProblem createdExamProblem, Exam exam) {
        return StudentAnswer.builder()
                .saAnswer(studentAnswerRequest.saAnswer())
                .saIsCorrect(isCorrect)
                .saSolvingTime(studentAnswerRequest.saSolvingTime())
                .examProblem(examProblem)
                .classroomStudent(classroomStudent)
                .createdExamProblem(createdExamProblem)
                .exam(exam)
                .build();
    }

    private Classroom loadClassroomByMemberRole(Long classroomMemberNo, MemberRole memberRole) {
        Classroom classroom;
        if (memberRole == MemberRole.STUDENT) {
            ClassroomStudent classroomStudent = loadClassroomStudent(classroomMemberNo);
            classroom = classroomStudent.getClassRoom();
        } else {
            ClassroomTeacher classroomTeacher = loadClassroomTeacher(classroomMemberNo);
            classroom = classroomTeacher.getClassroom();
        }
        return classroom;
    }

    private ClassroomTeacher loadClassroomTeacher(Long classroomMemberNo) {
        return classroomTeacherRepository.findById(classroomMemberNo).orElseThrow(
                () -> new IllegalArgumentException("잘못된 클래스룸 교사 고유번호입니다. : " + classroomMemberNo)
        );
    }

    private ExamProblem loadExamProblem(Long epNo) {
        return examProblemRepository.findById(epNo).orElseThrow(
                () -> new IllegalArgumentException("잘못된 시험문제 고유번호입니다. : " + epNo)
        );
    }

    private Exam loadExam(Long examNo) {
        return examRepository.findById(examNo).orElseThrow(
                () -> new IllegalArgumentException("잘못된 시험 고유번호입니다. : " + examNo)
        );
    }

    private StudentExam loadStudentExam(ClassroomStudent classroomStudent, Exam exam) {
        return studentExamRepository.findStudentExamByClassroomStudentAndExam(classroomStudent, exam);
    }

    private Map<Long, CreatedExamProblem> loadCreatedExamProblems(List<StudentAnswerRequest> requests) {
        Set<Long> cepNos = requests.stream()
                .map(StudentAnswerRequest::cepNo)
                .collect(Collectors.toSet());
        List<CreatedExamProblem> createdExamProblemList = createdExamProblemRepository.findAllById(cepNos);
        return createdExamProblemList.stream()
                .collect(Collectors.toMap(CreatedExamProblem::getCepNo, cep -> cep));
    }

    private Map<Long, ExamProblem> loadExamProblems(final List<StudentAnswerRequest> requests) {
        Set<Long> epNos = requests.stream()
                .map(StudentAnswerRequest::epNo)
                .collect(Collectors.toSet());

        List<ExamProblem> examProblemList = examProblemRepository.findExamProblemsByEpNoIn(epNos);
        return examProblemList.stream()
                .collect(Collectors.toMap(ExamProblem::getEpNo, ep -> ep));
    }

    private ClassroomStudent loadClassroomStudent(Long classroomStudentNo) {
        return classroomStudentRepository.findById(classroomStudentNo).orElseThrow(
                () -> new IllegalArgumentException("잘못된 클래스룸학생 고유번호입니다. : " + classroomStudentNo)
        );
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

    private void addRandomProblemsByUnitAndLevelWithCount(ExamProblemRequest examProblemRequest, List<ExamProblemResponse> examProblemResponseList, Long unitNo) {
        for (Map.Entry<ProblemLevel, Integer> entry : examProblemRequest.problemCountsByLevel().entrySet()) {
            ProblemLevel problemLevel = entry.getKey();
            Integer count = entry.getValue();
            examProblemResponseList.addAll(examProblemRepository.findRandomExamProblemsByUnitAndLevel(unitNo, problemLevel, PageRequest.of(0, count)));
        }
    }

    private void addExamProblemToExam(final List<Long> epNoList, final Exam exam) {
        List<ExamProblem> examProblemList = examProblemRepository.findAllById(epNoList);
        //랜덤하게 문제 배치를 위한 컬렉션 요소 셔플
        Collections.shuffle(examProblemList);
        //낮은 난이도가 먼저 배치될 수 있도록 난이도별 정렬
        examProblemList.sort(Comparator.comparingInt(ep -> switch (ep.getEpLevel()) {
            case LOW -> 0;
            case MEDIUM -> 1;
            case HIGH -> 2;
        }));

        Map<Long, ExamProblem> examProblemMap = examProblemList.stream().collect(Collectors.toMap(ExamProblem::getEpNo, e -> e));
        int order = 0;
        for (ExamProblem examProblem : examProblemList) {

            ExamProblem ep = examProblemMap.get(examProblem.getEpNo());
            if (ep == null) {
                throw new IllegalArgumentException("존재하지 않는 시험문제 번호입니다. : " + examProblem.getEpNo());
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

    private record Result(Duration totalSolvingTime, int roundScore) {

    }

}




















