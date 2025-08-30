package com.airoom.airoom.aichat.model.service;

import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.exam.model.repository.StudentExamRepository;
import com.airoom.airoom.member.model.repository.MemberRepository;
import com.airoom.airoom.statistic.model.repository.LearningSummaryRepository;
import com.airoom.airoom.statistic.model.repository.UnitSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class StudentContextService {
    private final MemberRepository memberRepo;
    private final ClassroomStudentRepository csRepo;
    private final StudentExamRepository seRepo;
    private final LearningSummaryRepository lsRepo;
    private final UnitSummaryRepository usRepo;

    /*
    가치 있는 정보(간단·안정·효과 순):
        회원 프로필(필수): Member.memberNo/Name/School/Gender/Grade/Class
        최근 시험/점수(있으면): StudentExam(seScore, seIsDone, seEndTime), 최근 1개
        학습 요약(있으면): LearningSummary 최근 일/월 단위 1개(학습일수, 총학습시간, 정답률)
        단원 성과(선택): UnitSummary 상위 정확도/시간 단원 1~2개
        클래스룸 정보(선택): ClassroomStudent → Classroom(학교/학년/반 중복 확인)
    */

    @Transactional(readOnly = true)
    public Map<String, Object> build(Long memberNo) {
        Map<String,Object> out = new LinkedHashMap<>();

        memberRepo.findById(memberNo).ifPresent(m -> {
            out.put("memberNo", m.getMemberNo());
            out.put("name", m.getMemberName());
            out.put("school", m.getMemberSchool());
            out.put("gender", m.getMemberGender());
            out.put("grade", m.getMemberGrade());
            out.put("class", m.getMemberClass());
        });

        csRepo.findTopByStudent_MemberNoOrderByCreatedAtDesc(memberNo)
                .ifPresent(cs -> {
                    out.put("classroomNo", cs.getClassRoom().getClassroomNo());
                    out.put("classroomGrade", cs.getClassRoom().getClassroomGrade());
                });

        seRepo.findTopByClassroomStudent_Student_MemberNoOrderByCreatedAtDesc(memberNo)
                .ifPresent(se -> {
                    out.put("recentExamScore", se.getSeScore());
                    out.put("recentExamDone", se.getSeIsDone());
                    out.put("recentExamEnd", se.getSeEndTime());
                });

        // 최근 요약(월 단위 우선)
        lsRepo.findTopById_LsClassroomStudentNoOrderByCreatedAtDesc(
                        latestClassroomStudentNo(memberNo))
                .ifPresent(ls -> {
                    out.put("summaryLearningDays", ls.getLsTotalLearningDays());
                    out.put("summaryLearningTimeMs", ls.getLsTotalLearningTimeMs());
                    out.put("summaryAccuracyRate", ls.getLsAccuracyRate());
                });

        // 단원 Top (정확도/시간 기준 1~2개)
        var topUnits = usRepo.findTop2ById_UsClassroomStudentNoOrderByUsAccuracyRateDesc(
                latestClassroomStudentNo(memberNo));
        if (!topUnits.isEmpty()) out.put("topUnits", topUnits);

        return out;
    }

    private Long latestClassroomStudentNo(Long memberNo){
        return csRepo.findTopByStudent_MemberNoOrderByCreatedAtDesc(memberNo)
                .map(cs -> cs.getClassRoomStudentNo()).orElse(null);
    }
}
